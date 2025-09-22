package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 16.
 *
 *   SSE 연결 후 CONNECTED 이벤트로 내려오는 응답 규격.
 */
data class SseConnectedResponse(

    @SerializedName("matrixPushId")
    val matrixPushId: String
)
