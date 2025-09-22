package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName
import matrix.push.client.modules.database.ClientStatus

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 *
 *   클라이언트 메시지 수신 상태 업데이트 시 사용 할 data.
 */
data class MessageStatusRequest(

    @SerializedName("matrixPushId")
    val matrixPushId: String,

    @SerializedName("updateData")
    val updateData: List<UpdateDataItem>

) {
    data class UpdateDataItem(
        @SerializedName("pushDispatchId")
        val pushDispatchId: String,

        @SerializedName("status")
        val status: ClientStatus
    )
}
