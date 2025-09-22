package matrix.push.client.usecase

import matrix.commons.log.MatrixLog
import matrix.push.client.modules.network.ApiException
import matrix.push.client.usecase.observer.PushEventObserver
import matrix.push.client.usecase.repository.RegisterRepository
import matrix.push.client.usecase.repository.SseRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
internal class ServerConnectUseCase(
    private val deviceId: String,
    private val appIdentifier: String,
    private val sseRepository: SseRepository,
    private val registerRepository: RegisterRepository,
    private val pushEventObserver: PushEventObserver,
) {

    companion object {
        private const val TAG = "ServerConnectUseCase"
    }

    /**
     * UseCase의 역할을 명확히 함: 오직 SSE 연결만 시도.
     * 기기 등록을 시도하고, 성공 시 받은 rId로 SSE 연결을 시작한다.
     * @return 연결 성공 시 matrixPushId, 실패 시 빈 문자열 또는 Exception 발생
     */
    suspend fun connectSseOnly(): String {
        // 1. 기기 등록 API 호출
        val response = registerRepository.register()
        val rId = response.body()?.rId

        if (rId != null) {
            return connectSse(rId)
        } else {
            val code = response.code()
            val errorBody = response.errorBody()?.string()
            MatrixLog.e(TAG, "Register API failed for SSE: code=$code, body=$errorBody")
            throw ApiException("Failed to register device for SSE", code, errorBody)
        }
    }

    /**
     * SseManager.connect를 호출하는 private 헬퍼 함수
     */
    private suspend fun connectSse(rId: String): String {

        // 1. SSE 연결 시작 요청.
        val matrixPushId = sseRepository.connect(
            rId = rId,
            deviceId = deviceId,
            appIdentifier = appIdentifier,
            onTokenRefreshNeeded = {
                // ★★★ 토큰 갱신 시, register()를 다시 호출하여 새 rId를 받아옴
                val refreshResponse = registerRepository.register()
                refreshResponse.body()?.rId // 성공하면 새 rId, 실패하면 null 반환
            }
        )

        // 2. SSE 이벤트 스트림 구독 시작.
        if (matrixPushId.isNotEmpty()) {
            pushEventObserver.startObserving()
        }

        return matrixPushId
    }

    /**
     * SSE 연결을 종료하는 함수
     */
    fun disconnectSse() {
        sseRepository.disconnect()
    }
}