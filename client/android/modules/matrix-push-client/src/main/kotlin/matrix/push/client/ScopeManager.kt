package matrix.push.client

import matrix.commons.event.SingleCoroutineDispatcher
import matrix.commons.log.MatrixLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * @author tarkarn
 * @since 2025.05.19
 */
object ScopeManager {

    private const val TAG = "ScopeManager"

    /**
     * 전역 코루틴 예외 핸들러
     * 네트워크 연결 오류나 기타 예외가 발생해도 앱이 크래시되지 않도록 처리
     */
    private val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is ConnectException -> {
                MatrixLog.e(TAG, "Network connection failed: ${exception.message}", exception)
            }
            is SocketTimeoutException -> {
                MatrixLog.e(TAG, "Network timeout occurred: ${exception.message}", exception)
            }
            else -> {
                MatrixLog.e(TAG, "Unhandled coroutine exception: ${exception.message}", exception)
            }
        }
    }

    /**
     * push client 기본 coroutine.
     */
    private val pushClientContext = SingleCoroutineDispatcher("mps-service")
    internal val pushClientScope = CoroutineScope(SupervisorJob() + pushClientContext + globalExceptionHandler)

    /**
     * push client database 전용 coroutine.
     */
    private val databaseContext = SingleCoroutineDispatcher("mps-db")
    internal val databaseScope = CoroutineScope(SupervisorJob() + databaseContext + globalExceptionHandler)

    /**
     * push sse 전용 coroutine.
     */
    private val sseContext = SingleCoroutineDispatcher("mps-sse")
    internal val sseScope = CoroutineScope(SupervisorJob() + sseContext + globalExceptionHandler)

    /**
     * ScopeManager가 관리하는 모든 코루틴 스코프를 취소한다.
     * SDK가 종료될 때 호출되어 모든 백그라운드 작업을 안전하게 중지시킨다.
     */
    internal fun cancelAll() {
        MatrixLog.i(TAG, "Cancelling all managed coroutine scopes...")

        // 각각의 스코프를 취소하여 관련된 모든 코루틴을 중단
        pushClientScope.cancel("SDK is shutting down.")
        databaseScope.cancel("SDK is shutting down.")
        sseScope.cancel("SDK is shutting down.")

        MatrixLog.i(TAG, "All scopes have been cancelled.")
    }
}