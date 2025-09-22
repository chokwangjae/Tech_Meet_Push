package matrix.push.client.usecase.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
interface SseRepository {

    /**
     * SSE 연결 상태를 관찰할 수 있는 StateFlow.
     */
    val connectionStatus: StateFlow<SseStatus>

    /**
     * 이벤트 전달 용.
     */
    val events: SharedFlow<String>

    /**
     * SSE 연결을 시작한다.
     * @param rId 초기 연결에 사용할 rId.
     * @param deviceId 기기 ID.
     * @param appIdentifier 앱 식별자.
     * @param onTokenRefreshNeeded 401 에러 발생 시 새 rId를 가져오기 위해 호출될 람다.
     */
    suspend fun connect(
        rId: String,
        deviceId: String,
        appIdentifier: String,
        onTokenRefreshNeeded: suspend () -> String?
    ): String

    /**
     * SSE 연결을 명시적으로 종료한다.
     */
    fun disconnect()
}