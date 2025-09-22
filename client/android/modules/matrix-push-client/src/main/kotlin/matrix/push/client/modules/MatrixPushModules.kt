package matrix.push.client.modules

import android.content.Context
import com.google.gson.Gson
import matrix.commons.datastore.DataStoreRepository
import matrix.commons.datastore.PreferenceKeys
import matrix.commons.datastore.matrixPushDatastore
import matrix.commons.log.MatrixLog
import matrix.commons.utils.Utils
import matrix.push.client.MatrixPushClientOptions
import matrix.push.client.PushMode
import matrix.push.client.ScopeManager
import matrix.push.client.modules.database.DatabaseModules
import matrix.push.client.modules.network.ApiExecutor
import matrix.push.client.modules.network.NetworkModules
import matrix.push.client.usecase.DeleteMessageUseCase
import matrix.push.client.usecase.FetchCampaignListUseCase
import matrix.push.client.usecase.GetAllMessagesUseCase
import matrix.push.client.usecase.GetErrorMessageUseCase
import matrix.push.client.usecase.GetMessagesByIdUseCase
import matrix.push.client.usecase.LoginUseCase
import matrix.push.client.usecase.LogoutUseCase
import matrix.push.client.usecase.MarkMessageAsConfirmedUseCase
import matrix.push.client.usecase.MarkMessageAsReceivedUseCase
import matrix.push.client.usecase.ObserveMessagesUseCase
import matrix.push.client.usecase.ProcessPushMessageUseCase
import matrix.push.client.usecase.RetryReportMessageStatusUseCase
import matrix.push.client.usecase.ServerConnectUseCase
import matrix.push.client.usecase.SyncMessageUseCase
import matrix.push.client.usecase.SyncPushModeUseCase
import matrix.push.client.usecase.UpdateCampaignConsentUseCase
import matrix.push.client.usecase.UpdateNewFcmTokenUseCase
import matrix.push.client.usecase.UpdateUserConsentUseCase
import matrix.push.client.usecase.observer.PushEventObserver
import matrix.push.client.usecase.repository.CampaignImpl
import matrix.push.client.usecase.repository.CampaignRepository
import matrix.push.client.usecase.repository.LogoutImpl
import matrix.push.client.usecase.repository.LogoutRepository
import matrix.push.client.usecase.repository.NotificationImpl
import matrix.push.client.usecase.repository.NotificationRepository
import matrix.push.client.usecase.repository.PushMessageImpl
import matrix.push.client.usecase.repository.PushMessageRepository
import matrix.push.client.usecase.repository.RegisterImpl
import matrix.push.client.usecase.repository.RegisterRepository
import matrix.push.client.usecase.repository.SseImpl
import matrix.push.client.usecase.repository.SseRepository
import matrix.push.client.usecase.repository.SseStatus

internal object MatrixPushModules {

    private const val TAG = "MatrixPushModules"

    var isInitialized = false

    private lateinit var applicationContext: Context
    private lateinit var datastore: DataStoreRepository
    private val gson: Gson by lazy { Gson() }
    private lateinit var serverUrl: String
    private lateinit var deviceId: String
    private lateinit var appIdentifier: String

    private lateinit var apiExecutor: ApiExecutor

    /**
     * repository
     */
    private lateinit var registerRepository: RegisterRepository
    private lateinit var sseRepository: SseRepository
    private lateinit var pushMessageRepository: PushMessageRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var markMessageAsConfirmedUseCase: MarkMessageAsConfirmedUseCase
    private lateinit var markMessageAsReceivedUseCase: MarkMessageAsReceivedUseCase
    private lateinit var observeMessagesUseCase: ObserveMessagesUseCase
    private lateinit var getAllMessagesUseCase: GetAllMessagesUseCase
    private lateinit var getMessagesByIdUseCase: GetMessagesByIdUseCase
    private lateinit var campaignRepository: CampaignRepository
    private lateinit var logoutRepository: LogoutRepository

    /**
     * useCase
     */
    private lateinit var serverConnectUseCase: ServerConnectUseCase
    private lateinit var processPushMessageUseCase: ProcessPushMessageUseCase
    private lateinit var syncMessageUseCase: SyncMessageUseCase
    private lateinit var fetchCampaignListUseCase: FetchCampaignListUseCase
    private lateinit var updateUserConsentUseCase: UpdateUserConsentUseCase
    private lateinit var updateCampaignConsentUseCase: UpdateCampaignConsentUseCase
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var updateNewFcmTokenUseCase: UpdateNewFcmTokenUseCase
    private lateinit var deleteMessageUseCase: DeleteMessageUseCase
    private lateinit var retryReportMessageStatusUseCase: RetryReportMessageStatusUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var syncPushModeUseCase: SyncPushModeUseCase
    private lateinit var getErrorMessageUseCase: GetErrorMessageUseCase

    /**
     * observer
     */
    private lateinit var pushEventObserver: PushEventObserver

    internal fun inject(context: Context, serverUrl: String) {

        if (isInitialized) {
            MatrixLog.w(TAG, "Push Modules is already initialized. Ignoring subsequent inject call.")
            return
        }

        // default config.
        this.applicationContext = context.applicationContext
        this.datastore = DataStoreRepository(applicationContext.matrixPushDatastore)
        this.deviceId = Utils.getDeviceUUID(applicationContext)
        this.appIdentifier = applicationContext.packageName

        MatrixPushClientOptions.serverUrl = serverUrl

        // repository.

        this.notificationRepository = NotificationImpl(
            applicationContext,
            DatabaseModules.pushMessagesDao,
            gson
        )

        this.registerRepository = RegisterImpl(deviceId, appIdentifier)

        this.loginUseCase = LoginUseCase(
            registerRepository,
            NetworkModules.pushService,
            datastore,
            deviceId,
            appIdentifier
        )

        // 중요: login 재시도를 위한 executor 이므로 loginUseCase 이후에 초기화 되어야 함.
        this.apiExecutor = ApiExecutor(
            reLoginUseCase = { loginUseCase.reLogin() },
            datastore = datastore
        )

        this.syncPushModeUseCase = SyncPushModeUseCase(registerRepository)

        // matrixPushId (accessToken) 이 필요한 API 호출이 있는 repository 들.
        this.pushMessageRepository = PushMessageImpl(
            deviceId,
            appIdentifier,
            DatabaseModules.pushMessagesDao,
            apiExecutor,
            NetworkModules.pushService,
            datastore,
            gson
        )

        this.campaignRepository = CampaignImpl(NetworkModules.pushService, apiExecutor)

        this.sseRepository = SseImpl(
            NetworkModules.pushService,
            apiExecutor,
            ScopeManager.sseScope,
            datastore,
            gson
        )

        this.logoutRepository = LogoutImpl(
            deviceId,
            appIdentifier,
            NetworkModules.pushService,
            apiExecutor
        )


        this.observeMessagesUseCase = ObserveMessagesUseCase(pushMessageRepository)
        this.getAllMessagesUseCase = GetAllMessagesUseCase(pushMessageRepository)
        this.getMessagesByIdUseCase = GetMessagesByIdUseCase(pushMessageRepository)
        this.getErrorMessageUseCase = GetErrorMessageUseCase(pushMessageRepository)

        this.markMessageAsConfirmedUseCase = MarkMessageAsConfirmedUseCase(pushMessageRepository)
        this.markMessageAsReceivedUseCase = MarkMessageAsReceivedUseCase(pushMessageRepository)

        this.fetchCampaignListUseCase = FetchCampaignListUseCase(campaignRepository)
        this.updateUserConsentUseCase = UpdateUserConsentUseCase(campaignRepository)
        this.updateCampaignConsentUseCase = UpdateCampaignConsentUseCase(campaignRepository)

        this.processPushMessageUseCase = ProcessPushMessageUseCase(
            pushMessageRepository,
            notificationRepository
        )

        this.updateNewFcmTokenUseCase = UpdateNewFcmTokenUseCase(registerRepository)
        this.deleteMessageUseCase = DeleteMessageUseCase(pushMessageRepository)
        this.retryReportMessageStatusUseCase = RetryReportMessageStatusUseCase(pushMessageRepository)
        this.logoutUseCase = LogoutUseCase(logoutRepository, ScopeManager.pushClientScope)

        // observer
        this.pushEventObserver = PushEventObserver(
            sseRepository,
            processPushMessageUseCase,
            ScopeManager.pushClientScope,
            ScopeManager.sseScope,
            gson
        )

        // useCase 지만 observer 이후에 초기화가 되어야 함.
        this.serverConnectUseCase = ServerConnectUseCase(
            deviceId,
            appIdentifier,
            sseRepository,
            registerRepository,
            pushEventObserver
        )

        this.syncMessageUseCase = SyncMessageUseCase(
            pushMessageRepository = pushMessageRepository,
            pushMessagesDao = DatabaseModules.pushMessagesDao,
            pushEventObserver = pushEventObserver
        )

        isInitialized = true
    }

    internal fun isInitialized(): Boolean {
        return this.isInitialized()
    }

    internal fun getApplicationContext(): Context {
        return this.applicationContext
    }

    internal fun getDataStore(): DataStoreRepository {
        return this.datastore
    }

    internal fun getServerUrl(): String {
        return this.serverUrl
    }

    internal suspend fun getMatrixPushId(): String {
        return datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")
    }

    internal fun getServerConnectUseCase(): ServerConnectUseCase {
        return this.serverConnectUseCase
    }

    internal fun getSyncMessageUseCase(): SyncMessageUseCase {
        return this.syncMessageUseCase
    }

    internal fun getPushEventObserver(): PushEventObserver {
        return this.pushEventObserver
    }

    // processPushMessageUseCase
    internal fun getProcessPushMessageUseCase(): ProcessPushMessageUseCase {
        return this.processPushMessageUseCase
    }

    internal fun getMarkMessageAsConfirmedUseCase(): MarkMessageAsConfirmedUseCase {
        return this.markMessageAsConfirmedUseCase
    }

    internal fun getMarkMessageAsReceivedUseCase(): MarkMessageAsReceivedUseCase {
        return this.markMessageAsReceivedUseCase
    }

    internal fun getObserveMessagesUseCase(): ObserveMessagesUseCase {
        return this.observeMessagesUseCase
    }

    internal fun getAllMessagesUseCase(): GetAllMessagesUseCase {
        return this.getAllMessagesUseCase
    }

    internal fun getMessageById(): GetMessagesByIdUseCase {
        return this.getMessagesByIdUseCase
    }

    internal fun getErrorMessageUseCase(): GetErrorMessageUseCase {
        return this.getErrorMessageUseCase
    }

    internal fun getCampaignListUseCase(): FetchCampaignListUseCase {
        return this.fetchCampaignListUseCase
    }

    internal fun getUpdateUserConsentUseCase(): UpdateUserConsentUseCase {
        return this.updateUserConsentUseCase
    }

    internal fun getUpdateCampaignConsentUseCase(): UpdateCampaignConsentUseCase {
        return this.updateCampaignConsentUseCase
    }

    internal fun getLoginUseCase(): LoginUseCase {
        return this.loginUseCase
    }

    internal fun getUpdateNewFcmTokenUseCase(): UpdateNewFcmTokenUseCase {
        return this.updateNewFcmTokenUseCase
    }

    internal fun getDeletePushMessageUseCase(): DeleteMessageUseCase {
        return this.deleteMessageUseCase
    }

    internal fun getRetryReportMessageStatusUseCase(): RetryReportMessageStatusUseCase {
        return this.retryReportMessageStatusUseCase
    }

    internal fun getLogoutUseCase(): LogoutUseCase {
        return this.logoutUseCase
    }

    internal fun getSyncPushModeUseCase(): SyncPushModeUseCase {
        return this.syncPushModeUseCase
    }

    internal suspend fun getPushMode(): PushMode? {
        val pushMode = datastore.getData(PreferenceKeys.Push.PUSH_MODE, "")
        return try {
            PushMode.valueOf( pushMode)
        } catch (e: IllegalArgumentException) {
            MatrixLog.e(TAG, "Invalid PushMode value in datastore: $pushMode")
            null
        }
    }

    internal fun isReady(): Boolean {
        if (!isInitialized) {
            MatrixLog.e(TAG, "Push Client is not initialized.")
        }

        return isInitialized
    }

    /**
     * 현재 SSE가 'Connected' 상태인지 확인.
     * @return 연결되어 있으면 true, 아니면 false.
     */
    internal fun isSseConnected(): Boolean {
        if (!isInitialized) return false

        // sseRepository의 현재 connectionStatus 값을 확인
        return sseRepository.connectionStatus.value is SseStatus.Connected
    }

    internal fun reset() {
        sseRepository.disconnect()

        ScopeManager.cancelAll()

        isInitialized = false
    }
}