package matrix.push.client.data

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 *
 *   외부 개발자에게 제공될 메시지 데이터 규격.
 */
data class PushMessage(
    val pushDispatchId: String,
    val messageType: String,
    val title: String?,
    val body: String?,
    val imageUrl: String?,
    val campaignId: String?,
    val payload: String?,   // JSON String
    val receivedAt: Long,   // Unix Timestamp (Long)
    val status: String      // "RECEIVED", "CONFIRMED", "ERROR"
)