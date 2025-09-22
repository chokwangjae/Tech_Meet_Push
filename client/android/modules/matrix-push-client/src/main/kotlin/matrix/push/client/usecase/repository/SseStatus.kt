package matrix.push.client.usecase.repository

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
sealed class SseStatus {
    object Connected : SseStatus()
    object Disconnected : SseStatus()
    data class Error(val message: String?, val cause: Throwable?) : SseStatus()
}