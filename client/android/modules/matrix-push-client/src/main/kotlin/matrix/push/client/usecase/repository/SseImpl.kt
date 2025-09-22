package matrix.push.client.usecase.repository

import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import matrix.commons.datastore.DataStoreRepository
import matrix.commons.datastore.PreferenceKeys
import matrix.commons.log.MatrixLog
import matrix.push.client.MatrixPushClientOptions
import matrix.push.client.modules.error.MpsError
import matrix.push.client.modules.error.MpsException
import matrix.push.client.modules.network.ApiExecutor
import matrix.push.client.modules.network.NetworkModules
import matrix.push.client.modules.network.PushService
import matrix.push.client.modules.network.data.HealthCheckEventData
import matrix.push.client.modules.network.data.HealthCheckRequest
import matrix.push.client.modules.network.data.SseConnectedResponse
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.atomic.AtomicBoolean

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 *
 *   SSE 관련 기능 구현체.
 */
internal class SseImpl(
    private val pushService: PushService,
    private val apiExecutor: ApiExecutor,
    private val scope: CoroutineScope,
    private val datastore: DataStoreRepository,
    private val gson: Gson
) : SseRepository {

    private companion object Companion {
        private const val TAG = "SseRepositoryImpl"
    }

    private var eventSource: EventSource? = null

    /**
     * 연결 상태 관리
     */
    private val _connectionStatus = MutableStateFlow<SseStatus>(SseStatus.Disconnected)
    override val connectionStatus = _connectionStatus.asStateFlow()

    // override 키워드를 사용하여 인터페이스의 events 프로퍼티를 구현
    private val _events = MutableSharedFlow<String>()
    override val events: SharedFlow<String> = _events.asSharedFlow()

    /**
     * 재연결에 필요한 파라미터 저장용 변수
     */
    private var lastRId: String? = null
    private var lastDeviceId: String? = null
    private var lastAppIdentifier: String? = null

    /**
     * 토큰 갱신 로직을 위해 사용.
     */
    private var tokenRefresher: (suspend () -> String?)? = null

    /**
     * 재연결 시도를 제어하기 위한 Job
     */
    private var reconnectionJob: Job? = null

    /**
     * 의도적으로 연결을 종료한 것인지 판별하기 위한 flag.
     */
    private val isManuallyDisconnected = AtomicBoolean(false)

    private var connectionCompleter: CompletableDeferred<String?>? = null
    private val connectionMutex = Mutex()

    /**
     * SSE 연결 시작
     */
    override suspend fun connect(
        rId: String,
        deviceId: String,
        appIdentifier: String,
        onTokenRefreshNeeded: suspend () -> String?
    ): String {
        try {
            // 이미 연결 중이거나, 재연결 시도 중이라면 중복 실행 방지
            if (_connectionStatus.value is SseStatus.Connected || reconnectionJob?.isActive == true) {
                MatrixLog.d(TAG, "Already connected or reconnection is in progress.")
                val existingId = datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")
                if (existingId.isNotEmpty()) return existingId
            }

            // 재연결을 위해 파라미터 저장 및 플래그 설정
            this.lastRId = rId
            this.lastDeviceId = deviceId
            this.lastAppIdentifier = appIdentifier
            this.tokenRefresher = onTokenRefreshNeeded
            this.isManuallyDisconnected.set(false)

            // 기존 재연결 시도가 있었다면 취소
            reconnectionJob?.cancel()

            // 최초 연결 결과를 위한 Deferred 초기화 (앱 종료 시 연결 시점을 확실히 하기 위ham)
            connectionCompleter = CompletableDeferred()

            // 실제 연결 로직 실행
            performConnection()

            // 최초 연결 결과를 대기 (10초 타임아웃)
            val result = withTimeoutOrNull(MatrixPushClientOptions.sseConnectionTimeoutMs) {
                connectionCompleter?.await()
            }

            // 대기가 끝나면 Completer는 더 이상 필요 없으므로 null로 설정
            connectionCompleter = null

            return result ?: throw MpsException(MpsError.SSE_CONNECTION_FAILED, "Connection timed out after ${MatrixPushClientOptions.sseConnectionTimeoutMs}ms")
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to connect to SSE", e)
            throw e as? MpsException ?: MpsException(MpsError.SSE_CONNECTION_FAILED, e.message, e)
        }
    }

    /**
     * SSE 연결 종료
     */
    override fun disconnect() {
        MatrixLog.d(TAG, "SSE connection closing...")

        isManuallyDisconnected.set(true)    // 수동으로 연결 끊음.
        reconnectionJob?.cancel()

        eventSource?.cancel()
        eventSource = null
        _connectionStatus.value = SseStatus.Disconnected

        lastRId = null
        lastDeviceId = null
        lastAppIdentifier = null
        tokenRefresher = null
    }

    /**
     * 재연결 시도 처리를 위해 SSE 연결 실제 로직 구현 분리.
     */
    private suspend fun performConnection() {
        connectionMutex.withLock {
            // 이미 연결되어 있다면 중복 실행 방지
            if (_connectionStatus.value is SseStatus.Connected && !isManuallyDisconnected.get()) {
                MatrixLog.d(TAG, "Already connected. performConnection is aborted.")
                return@withLock
            }

            if (eventSource != null) {
                eventSource?.cancel()
                eventSource = null
            }

            val rId = lastRId ?: return@withLock
            val deviceId = lastDeviceId ?: return@withLock
            val appIdentifier = lastAppIdentifier ?: return@withLock

            val request = pushService.sseConnect(
                rId = rId,
                deviceId = deviceId,
                appIdentifier = appIdentifier
            ).request()
            val factory = EventSources.createFactory(NetworkModules.okHttpClient)
            val listener = createEventSourceListener()
            eventSource = factory.newEventSource(request, listener)
            MatrixLog.d(TAG, "SSE connection attempt initiated...")
        }
    }

    private fun createEventSourceListener(): EventSourceListener {
        return object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                reconnectionJob?.cancel()       // 연결 성공 시 재연결 시도 중단.
                _connectionStatus.value = SseStatus.Connected
                MatrixLog.d(TAG, "SSE connection opened.")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                MatrixLog.d(TAG, "New event id: $id, type: $type, data: $data")

                when (type) {

                    // 연결 성공 시.
                    "CONNECTED" -> {
                        connected(data)
                    }

                    // 연결 유지를 위한 health check.
                    "HEALTH_CHECK" -> {
                        healthCheck(data)
                    }

                    // private push 메시지 수신.
                    "PUSH_MESSAGE" -> {
                        scope.launch {
                            _events.emit(data)
                        }
                    }

                    // 연결 끊김.
                    "DISCONNECT" -> {
                        // 자동으로 onClosed 가 호출 됨.
                    }
                }
            }

            override fun onClosed(eventSource: EventSource) {
                _connectionStatus.value = SseStatus.Disconnected
                MatrixLog.d(TAG, "SSE connection closed.")

                // 연결 재시도 로직.
                if (!isManuallyDisconnected.get()) {
                    scheduleReconnection()
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val errorMessage = response?.message ?: t?.message ?: "Unknown SSE error"
                val exception = MpsException(MpsError.SSE_CONNECTION_FAILED, errorMessage, t)

                _connectionStatus.value = SseStatus.Error(errorMessage, exception)
                MatrixLog.e(TAG, "SSE connection failed: code=${response?.code}, msg=$errorMessage", exception)

                // 최초 연결 시도에 대한 실패를 알림.
                connectionCompleter?.completeExceptionally(exception)

                // 토큰 재발급 로직.
                if (response?.code == 401) {
                    handleAuthenticationFailure()
                }

                // 연결 재시도 로직.
                else if (!isManuallyDisconnected.get()) {
                    scheduleReconnection()
                }
            }
        }
    }

    /**
     * SSE 연결 완료 후 처리.
     */
    private fun connected(sseData: String) {
        try {
            val connectedResponse = gson.fromJson(sseData, SseConnectedResponse::class.java)
            val matrixPushId = connectedResponse.matrixPushId
            MatrixLog.d(TAG, "SSE CONNECTED | data: $connectedResponse")

            scope.launch {
                datastore.setData(PreferenceKeys.Push.MATRIX_PUSH_ID, matrixPushId)
            }

            // 연결 성공을 알림.
            connectionCompleter?.complete(matrixPushId)
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to parse CONNECTED event data", e)
            val exception = MpsException(MpsError.SSE_CONNECTION_FAILED, "Could not parse CONNECTED event data", e)
            connectionCompleter?.completeExceptionally(exception)
        }
    }

    /**
     * 연결 유지를 위한 health check.
     */
    private fun healthCheck(sseData: String) {
        try {
            val healthCheckData = gson.fromJson(sseData, HealthCheckEventData::class.java)
            MatrixLog.d(TAG, "SSE HEALTH_CHECK | data: $healthCheckData")

            scope.launch {
                try {
                    val matrixPushId = datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")
                    val response = apiExecutor.execute {
                        val requestBody = HealthCheckRequest(
                            matrixPushId = matrixPushId,
                            healthCheckId = healthCheckData.healthCheckId
                        )

                        pushService.healthCheck(requestBody)
                    }

                    if (!response.isSuccessful) {
                        val code = response.raw().code
                        val errorBody = response.errorBody()?.string()
                        MatrixLog.e(TAG, "healthCheck() | failed code: $code, body: $errorBody")
                        // Health check 실패는 연결을 끊고 재연결을 유도할 수 있음
                        eventSource?.cancel()
                    }
                } catch (e: Exception) {
                    MatrixLog.e(TAG, "Error during health check API call", e)
                    // API 호출 실패 시에도 연결 재시도 유도
                    eventSource?.cancel()
                }
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to parse HEALTH_CHECK event data", e)
        }
    }

    /**
     * 재연결 스케줄링 함수.
     */
    private fun scheduleReconnection() {
        if (reconnectionJob?.isActive == true) return
        MatrixLog.d(TAG, "Scheduling reconnection in ${MatrixPushClientOptions.sseReconnectionIntervalMs / 1000} seconds.")

        reconnectionJob = scope.launch {
            delay(MatrixPushClientOptions.sseReconnectionIntervalMs)
            MatrixLog.d(TAG, "Attempting to reconnect...")
            performConnection()
        }
    }

    /**
     * 401 UnAuthorized 발생 시 인증 갱신 처리.
     */
    private fun handleAuthenticationFailure() {
        if (reconnectionJob?.isActive == true) return

        reconnectionJob = scope.launch {
            MatrixLog.d(TAG, "Authentication token expired. Attempting to refresh rId...")

            // TODO: retry count 로 해야할지?
            delay(30000)    // 30초 간격 무제한 재시도.

            // 외부에서 주입받은 람다를 호출하여 새 rId를 요청
            val newRId = tokenRefresher?.invoke()

            if (newRId != null) {
                MatrixLog.d(TAG, "Successfully refreshed rId. Reconnecting immediately.")
                lastRId = newRId        // ★★★ 갱신된 rId로 교체
                performConnection()     // 즉시 재연결 시도
            } else {
                MatrixLog.e(TAG, "Failed to refresh rId. Stopping connection attempts.")
                val exception = MpsException(MpsError.SSE_CONNECTION_FAILED, "Permanent authentication failure: rId refresh failed")

                // 갱신 실패 시, 연결을 완전히 종료하고 더 이상 시도하지 않음
                disconnect()
                _connectionStatus.value = SseStatus.Error("Permanent authentication failure", exception)
                // 최초 연결 과정이었다면 실패 전파
                connectionCompleter?.completeExceptionally(exception)
            }
        }
    }
}