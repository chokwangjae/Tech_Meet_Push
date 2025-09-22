package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 */

// 미수신 메시지 동기화 시 사용 할 요청 규격.
data class MessageSyncRequest(

    @SerializedName("matrixPushId")
    val matrixPushId: String,

    @SerializedName("details")
    val details: MessageSyncDetails

) {
    data class MessageSyncDetails(

        @SerializedName("deviceId")
        val deviceId: String,

        @SerializedName("appIdentifier")
        val appIdentifier: String,

        @SerializedName("pushDispatchId")
        val pushDispatchId: String,

        @SerializedName("limit")
        val limit: String
    )
}

// 클라이언트 미수신 메시지 동기와 응답 규격.
data class MessageSyncResponse(
    @SerializedName("pushDispatchId") val pushDispatchId: String,
    @SerializedName("messageType") val messageType: String?,
    @SerializedName("messagePriority") val messagePriority: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("payload") val payload: String?,
    @SerializedName("sender") val sender: String?,
    @SerializedName("channelId") val channelId: String?,
    @SerializedName("channelName") val channelName: String?,
    @SerializedName("channelDescription") val channelDescription: String?,

    // FIXME: 서버에서 형식 변경 후 클라이언트와 맞출 것. (LocalDateTime.now())
    // yyyyMMddHHmmss 형식의 문자열
    @SerializedName("timestamp") val timestamp: String?
)