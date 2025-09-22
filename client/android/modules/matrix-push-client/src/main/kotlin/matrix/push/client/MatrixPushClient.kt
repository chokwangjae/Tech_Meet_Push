package matrix.push.client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import matrix.commons.datastore.PreferenceKeys
import matrix.commons.log.MatrixLog
import matrix.push.client.data.PushMessage
import matrix.push.client.modules.MatrixPushModules
import matrix.push.client.modules.database.DatabaseModules
import matrix.push.client.modules.error.MpsError
import matrix.push.client.modules.error.MpsException
import matrix.push.client.modules.network.data.UserCampaignGetResponse
import matrix.push.client.modules.network.data.UserConsentResponse
import matrix.push.client.usecase.OnErrorListener
import matrix.push.client.usecase.OnInitializedListener
import matrix.push.client.usecase.OnNewMessageListener
import matrix.push.client.usecase.OnSyncMessageCompleteListener
import java.util.Collections.emptyList

/**
 * @author tarkarn
 * @since 2025.05.16
 *
 * Matrix Push SDK의 메인 클라이언트 클래스입니다.
 * SDK의 초기화 및 푸시 토큰 관리를 담당합니다.
 *
 * 이 SDK를 사용하기 위해서는 [MatrixPushClient.builder]를 통해 [Builder] 인스턴스를 얻고,
 * 필요한 설정을 한 뒤 [Builder.build] 메서드를 호출하여 `MatrixPushClient` 인스턴스를 생성해야 합니다.
 * `build()` 메서드 호출 시 SDK 초기화가 자동으로 수행됩니다.
 *
 * 사용 예시:
 * ```kotlin
 * MatrixPushClient.builder(applicationContext, "YOUR_SERVER_URL")
 *     .userId("user123")
 *     .autoTokenRegistration(true)
 *     .configureService { config ->
 *         config.defaultChannelName = "My App Push"
 *         config.silentPushHandler = { context, data ->
 *             // 사일런트 푸시 처리 로직
 *         }
 *     }
 *     .build()
 * ```
 *
 * 기본 사용법 (configureService 없이):
 * ```kotlin
 * MatrixPushClient.builder(applicationContext, "YOUR_SERVER_URL")
 *     .userId("user123")
 *     .build()
 * ```
 *
 * @see Builder SDK 인스턴스를 생성하고 설정하기 위한 빌더 클래스.
 */
class MatrixPushClient() {

    private var onErrorListener: OnErrorListener? = null
    private var onInitializedListener: OnInitializedListener? = null

    companion object Companion {
        private const val TAG = "MatrixPushClient"

        /**
         * `MatrixPushClient` 인스턴스를 생성하기 위한 [Builder]를 반환.
         *
         * @param context 애플리케이션 [Context]. SDK 운영에 필수적입니다.
         *                일반적으로 애플리케이션의 `applicationContext`를 전달.
         * @param serverUrl 푸시 메시지 등록 및 기타 통신을 위한 서버의 기본 URL.
         * @return 새로운 [Builder] 인스턴스.
         */
        @JvmStatic
        fun builder(context: Context, serverUrl: String): Builder {
            return Builder(context.applicationContext, serverUrl)
        }
    }

    /**
     * `MatrixPushClient`를 구성하고 생성하기 위한 빌더 클래스.
     *
     * 필수 값인 `context`와 `serverUrl`은 빌더 생성 시 전달 필요,
     *
     * @constructor [Builder] 인스턴스를 생성.
     * @param context application [Context].
     * @param serverUrl 푸시 서버의 기본 URL.
     */
    class Builder(private val context: Context, private val serverUrl: String) {
        private var autoTokenRegistration: Boolean = true
        private var errorListener: OnErrorListener? = null
        private var initializedListener: OnInitializedListener? = null

        fun autoTokenRegistration(enable: Boolean): Builder = apply {
            this.autoTokenRegistration = enable
        }

        /**
         * 개발/디버그 환경 설정
         *
         * @param enable 디버그 모드 활성화 여부
         */
        fun debugMode(enable: Boolean): Builder = apply {
            MatrixLog.init(enable)
            if (enable) {
                MatrixLog.i(TAG, "Debug mode enabled for MatrixPushClient")
            }
        }

        /**
         * 초기화 완료 콜백 설정
         *
         * @param listener  초기화 완료 시 호출 될 콜백 함수
         */
        fun onInitialized(listener: OnInitializedListener): Builder = apply {
            this.initializedListener = listener
        }

        /**
         * 초기화 실패 및 각종 에러 콜백 설정
         *
         * @param listener 에러 발생 시 호출될 콜백 함수
         */
        fun onError(listener: OnErrorListener): Builder = apply {
            this.errorListener = listener
        }

        fun build(): MatrixPushClient? {
            return try {
                if (serverUrl.isEmpty()) {
                    errorListener?.onError(
                        MpsException(MpsError.INVALID_SERVER_URL, "serverUrl cannot be empty")
                    )
                    return null
                }
                if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                    errorListener?.onError(
                        MpsException(
                            MpsError.INVALID_SERVER_URL,
                            "serverUrl must start with http:// or https://"
                        )
                    )
                    return null
                }

                val client = MatrixPushClient()
                client.onErrorListener = this.errorListener
                client.onInitializedListener = this.initializedListener

                // 1. 모듈 주입 (네트워크 연결 X)
                client.inject(
                    context,
                    serverUrl,
                    autoTokenRegistration // 이 값은 이제 다른 곳에서 사용될 수 있습니다.
                )
                MatrixLog.i(TAG, "MatrixPushClient modules injected successfully.")

                // 2. enablePrivatePush 설정 저장
                client.setPushMode(
                    onSuccess = {
                        // 3. 자동 연결 시도 시
                        if (autoTokenRegistration) {
                            client.start()
                        } else {
                            initializedListener?.onInitialized(client)
                        }
                    },
                    onError = {
                        errorListener?.onError(it)
                    }
                )

                // 4. 알림 권한 체크 (연결과 무관)
                client.checkNotificationPermission(context, onCheckedPermission = { granted ->
                    if (!granted) {
                        val exception = MpsException(MpsError.PERMISSION_DENIED, "Notification permission not granted by user.")
                        errorListener?.onError(exception)
                    } else {
                        MatrixLog.i(TAG, "push client inject completed.")
                    }
                })


                // 5. 최근 초기화 정보 저장 (연결과 무관)
                client.saveInitializationInfo(serverUrl, autoTokenRegistration)

                // 6. 에러 메시지 조회 후 상태값 변경
                client.updateErrorMessagesToReceived()

                MatrixLog.i(TAG, "MatrixPushClient built successfully. Ready to connect or login.")
                client
            } catch (e: Exception) {
                MatrixLog.e(TAG, "MatrixPushClient build failed.", e)
                val mpsException = e as? MpsException ?: MpsException(MpsError.INITIALIZATION_FAILED, e.message, e)
                errorListener?.onError(mpsException)
                null
            }
        }
    }

    private fun inject(context: Context, serverUrl: String, autoTokenRegistration: Boolean) {

        // Database 모듈 inject
        DatabaseModules.inject(context)

        // 공통 모듈 inject.
        MatrixPushModules.inject(context, serverUrl)

    }

    /**
     * Push Mode Setting.
     */
    private fun setPushMode(onSuccess: () -> Unit, onError: (MpsException) -> Unit) {
        ScopeManager.pushClientScope.launch {
            try {
                // server 에 있는 Push 모드를 반환
                MatrixPushModules.getSyncPushModeUseCase().pushMode()
                onSuccess()
            } catch (mpe: MpsException) {
                onError(mpe)
            } catch (e: Exception) {
                MpsException(MpsError.REGISTER_FAILED, e.message ?: "").also {
                    onError(it)
                }
            }
        }
    }

    /**
     * Push 서비스를 시작한다.
     *
     * Push Client 를 초기화 하고 특정시점에 푸시 서비스를 시작해야 하는 경우,
     * 해당 함수를 통해 시작시킨다.
     *
     * autoRegistration 이 true 이면 자동으로 시작한다.
     */
    fun start() {
        ScopeManager.pushClientScope.launch {
            val pushMode = MatrixPushModules.getPushMode()

            when (pushMode) {
                PushMode.PUBLIC -> {
                    MatrixLog.i(TAG, "Starting anonymous login (Public Push).")
                    login { onInitializedListener?.onInitialized(this@MatrixPushClient) }
                }

                PushMode.PRIVATE -> {
                    MatrixLog.i(TAG, "Starting SSE connection (Private Push).")
                    connect { onInitializedListener?.onInitialized(this@MatrixPushClient) }
                }

                PushMode.ALL -> {
                    MatrixLog.i(TAG, "Starting SSE connection & anonymous login (All Push).")
                    val loginComplete = CompletableDeferred<Boolean>()
                    val connectCompleted = CompletableDeferred<Boolean>()

                    login { loginComplete.complete(true) }
                    connect { connectCompleted.complete(true) }

                    if (loginComplete.await() && connectCompleted.await()) {
                        onInitializedListener?.onInitialized(this@MatrixPushClient)
                    }
                }

                else -> {
                    throw MpsException(MpsError.INVALID_PUSH_MODE)
                }
            }

            // 모듈 인증 요청 30초 후 상태 미전송 한 메시지 서버에 전송 요청.
            delay(30 * 1000)
            retryReportMessageStatus()
        }
    }

    /**
     * Private Push (SSE) 연결을 시작합니다.
     * `enablePrivatePush(true)`로 빌드했을 때 사용해야 합니다.
     *
     * @param onResult 연결 시도 결과에 대한 콜백. 성공 시 null, 실패 시 Exception 전달.
     */
    fun connect(onConnected: () -> Unit = {}) {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized. Call build() first."))
            return
        }

        ScopeManager.pushClientScope.launch {
            try {
                // ServerConnectUseCase는 이제 순수하게 SSE 연결만 담당
                val matrixPushId = MatrixPushModules.getServerConnectUseCase().connectSseOnly()
                if (matrixPushId.isNotEmpty()) {
                    MatrixLog.i(TAG, "SSE connection successful.")
                    onConnected()
                } else {
                    throw MpsException(MpsError.SSE_CONNECTION_FAILED, "Could not obtain matrixPushId from server.")
                }
            } catch (e: Exception) {
                MatrixLog.e(TAG, "An exception occurred while connecting to SSE.", e)
                val mpsException = e as? MpsException ?: MpsException(MpsError.SSE_CONNECTION_FAILED, e.message, e)
                onErrorListener?.onError(mpsException)
            }
        }
    }

    /**
     * Public Push를 위해 사용자 정보를 등록하고 로그인합니다.
     * `enablePrivatePush(false)`로 빌드했을 때 사용해야 합니다.
     *
     * @param onResult 로그인 시도 결과에 대한 콜백. 성공 시 null, 실패 시 Exception 전달.
     */
    fun login(
        userId: String = "",
        userName: String = "",
        email: String = "",
        onLogin: () -> Unit = {}
    ) {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized. Call build() first."))
            return
        }

        ScopeManager.pushClientScope.launch {
            try {
                val matrixPushId = MatrixPushModules.getLoginUseCase().invoke(userId, userName, email)
                if (matrixPushId.isNotEmpty()) {
                    MatrixLog.i(TAG, "Login process completed successfully.")
                    onLogin()
                } else {
                    // 이 경우는 거의 없지만, 서버가 200 OK에 빈 ID를 주면 발생 가능
                    throw MpsException(MpsError.LOGIN_FAILED, "matrixPushId is empty after login.")
                }
            } catch (e: Exception) {
                MatrixLog.e(TAG, "Login failed.", e)
                val mpsException =
                    e as? MpsException ?: MpsException(MpsError.LOGIN_FAILED, e.message, e)
                onErrorListener?.onError(mpsException)
            }
        }
    }

    /**
     * 알림 권한 여부 체크.
     */
    private fun checkNotificationPermission(context: Context, onCheckedPermission: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context.applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    MatrixLog.i(TAG, "permission to post notifications has already been granted.")
                    onCheckedPermission.invoke(true)
                }

                else -> {
                    MatrixLog.e(TAG, "post notification permission denied.")
                    onCheckedPermission.invoke(false)
                }
            }
        }
    }

    /**
     * 최근 초기화가 성공했을떄의 정보 저장.
     * 앱 종료상태에서 푸시가 왔을 때, 모듈 초기화 시 사용.
     */
    private fun saveInitializationInfo(serverUrl: String, autoTokenRegistration: Boolean) {
        ScopeManager.pushClientScope.launch {
            MatrixPushModules.getDataStore().apply {
                setData(PreferenceKeys.Push.SERVER_URL, serverUrl)
                setData(PreferenceKeys.Push.AUTO_TOKEN_REGISTRATION, autoTokenRegistration)
                setData(PreferenceKeys.Push.IS_INITIALIZED_ONCE, true)
            }
        }
    }

    /**
     * 새로 수신되어 처리된 푸시 메시지에 대한 리스너 등록.
     * 이 리스너는 중복되지 않은 새로운 메시지가 DB에 저장되고 처리된 후에만 호출됨.
     *
     * @param listener 푸시 메시지를 받기위한 리스너
     */
    fun setOnNewMessageListener(listener: OnNewMessageListener) {
        try {
            MatrixPushModules.getPushEventObserver().setOnNewMessageListener(listener)
        } catch (e: IllegalStateException) {
            MatrixLog.e(TAG, "SDK not initialized. Cannot set OnNewMessageListener.", e)
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized.", e))
        }
    }

    /**
     * 미수신 메시지 동기화 완료 후 콜백 리스너.
     */
    fun setOnSyncMessageCompleteListener(listener: OnSyncMessageCompleteListener) {
        try {
            MatrixPushModules.getPushEventObserver().setOnSyncMessageCompleteListener(listener)
        } catch (e: IllegalStateException) {
            MatrixLog.e(TAG, "SDK not initialized. Cannot set setOnSyncMessageCompleteListener.", e)
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized.", e))
        }
    }

    /**
     * 미수신 메시지 동기화 요청.
     */
    fun syncMessages() {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized."))
            return
        }

        ScopeManager.pushClientScope.launch {
            try {
                MatrixPushModules.getSyncMessageUseCase().invoke()
            } catch (e: Exception) {
                val mpsException = e as? MpsException ?: MpsException(MpsError.SYNC_MESSAGES_FAILED, e.message, e)
                onErrorListener?.onError(mpsException)
            }
        }
    }

    /**
     * 'ERROR' 상태인 메시지 조회 후 '읽지 않은'(RECEIVED) 상태로 변경.
     */
    private fun updateErrorMessagesToReceived() {
        ScopeManager.pushClientScope.launch {
            MatrixPushModules.getErrorMessageUseCase().getErrorMessage().forEach {
                Log.d(TAG, "updateErrorMessagesToReceived :: $it")
                MatrixPushModules.getMarkMessageAsReceivedUseCase().invoke(
                    it.pushDispatchId
                )
            }
        }
    }

    /**
     * 특정 푸시 메시지를 '읽음'(CONFIRMED) 상태로 변경.
     * @param pushDispatchId 상태를 변경할 메시지의 고유 ID.
     */
    fun markMessageAsConfirmed(pushDispatchId: String) {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized."))
            return
        }

        ScopeManager.pushClientScope.launch {
            try {
                MatrixPushModules.getMarkMessageAsConfirmedUseCase()(pushDispatchId) // UseCase 호출
            } catch (e: Exception) {
                val mpsException = e as? MpsException ?: MpsException(MpsError.MARK_AS_CONFIRMED_FAILED, e.message, e)
                onErrorListener?.onError(mpsException)
            }
        }
    }

    /**
     * 모든 '읽지 않은' 메시지를 '읽음'(CONFIRMED) 상태로 변경.
     */
    fun markAllMessagesAsConfirmed() {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized."))
            return
        }

        ScopeManager.pushClientScope.launch {
            try {
                MatrixPushModules.getMarkMessageAsConfirmedUseCase()(null) // pushDispatchId를 null로 하여 UseCase 호출
            } catch (e: Exception) {
                val mpsException = e as? MpsException ?: MpsException(MpsError.MARK_AS_CONFIRMED_FAILED, e.message, e)
                onErrorListener?.onError(mpsException)
            }
        }
    }

    /**
     * 로컬에 저장된 모든 푸시 메시지 목록을 실시간으로 관찰
     * 메시지는 최근 수신 순으로 정렬.
     *
     * @return PushMessage 목록을 방출하는 Flow.
     */
    fun observeAllMessages(): Flow<List<PushMessage>> {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized."))
            return flowOf(emptyList())
        }
        return try {
            val useCase = MatrixPushModules.getObserveMessagesUseCase()

            useCase().map { entityList ->
                entityList.map { it.toPublicDto() }
            }
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.GET_MESSAGES_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            flowOf(emptyList())
        }
    }

    /**
     * 로컬에 저장된 모든 푸시 메시지 목록을 한 번만 조회합니다.
     *
     * @return PushMessage 목록.
     */
    suspend fun getAllMessages(): List<PushMessage> {
        if (!MatrixPushModules.isReady()) {
            onErrorListener?.onError(MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized."))
            return emptyList()
        }
        return try {
            val useCase = MatrixPushModules.getAllMessagesUseCase()
            val entityList = useCase()

            entityList.toPublicDtoList()
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.GET_MESSAGES_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            emptyList()
        }
    }

    /**
     * pushDispatchId 에 맞는 개별 메시지 조회
     *
     * @param pushDispatchId 조회할 메시지의 고유 ID.
     * @return 성공 시 메시지 정보, 실패 시 Exception 발생.
     */
    suspend fun getMessageById(pushDispatchId: String): PushMessage? {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }

        return try {
            val useCase = MatrixPushModules.getMessageById().getMessageById(pushDispatchId)
            useCase?.toPublicDto()
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.GET_MESSAGES_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
     * 사용자 campaign 목록 조회.
     * @return 성공 시 캠페인 목록, 실패 시 Exception 발생.
     */
    suspend fun getCampaignList(): List<UserCampaignGetResponse> {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }
        return try {
            // UseCase를 호출하고 결과를 바로 반환.
            MatrixPushModules.getCampaignListUseCase().invoke()
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.CAMPAIGN_INFO_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
    * 전체 푸시 수신 동의 여부를 업데이트하고, 변경된 전체 동의 상태와 캠페인 목록을 반환.
    */
    suspend fun updateUserConsent(consented: Boolean): UserConsentResponse {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }
        return try {
            MatrixPushModules.getUpdateUserConsentUseCase().invoke(consented)
        } catch (e: Exception) {
            val mpsException = if (e is MpsException) e else MpsException(MpsError.CAMPAIGN_INFO_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
     * 특정 캠페인의 푸시 수신 동의 여부를 업데이트하고, 변경된 전체 캠페인 목록을 반환.
     */
    suspend fun updateCampaignConsent(campaignId: Long, consented: Boolean): UserConsentResponse {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }
        return try {
            MatrixPushModules.getUpdateCampaignConsentUseCase().invoke(campaignId, consented)
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.CAMPAIGN_INFO_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
     * 특정 메시지를 삭제 처리 한다.
     */
    suspend fun deleteMessage(pushDispatchId: String) {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }
        try {
            MatrixPushModules.getDeletePushMessageUseCase().invoke(pushDispatchId)
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.DELETE_MESSAGE_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
     * 메시지 수신 상태 서버 전송 Retry
     * 수신, 삭제 처리 완료 후 서버에 상태 값 전달이 제대로 되지 않은 경우 재시도.
     *
     * 모듈 초기화 완료 후 30초 후 1회 시도.
     */
    private suspend fun retryReportMessageStatus() {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }

        try {
            MatrixPushModules.getRetryReportMessageStatusUseCase().invoke()
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.RETRY_MESSAGE_CALLBACK_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
     * 서버에 등록한 사용자 정보를 초기화 한다.
     * 현재 기기와 연결된 userId, name, email 정보를 초기화 시킨다.
     */
    fun logout() {
        if (!MatrixPushModules.isReady()) {
            val ex = MpsException(MpsError.INITIALIZATION_FAILED, "SDK not initialized")
            onErrorListener?.onError(ex)
            throw ex
        }

        try {
            MatrixPushModules.getLogoutUseCase().invoke()
        } catch (e: Exception) {
            val mpsException =
                e as? MpsException ?: MpsException(MpsError.LOGOUT_FAILED, e.message, e)
            onErrorListener?.onError(mpsException)
            throw mpsException
        }
    }

    /**
     * SDK의 모든 동작을 중지하고 리소스를 정리한다.
     * SSE 연결을 끊고, 진행 중인 모든 코루틴 작업을 취소한다.
     */
    fun shutdown() {
        MatrixLog.i(TAG, "Shutting down MatrixPushClient...")

        MatrixPushModules.reset()

        MatrixLog.i(TAG, "MatrixPushClient has been shut down.")
    }
}