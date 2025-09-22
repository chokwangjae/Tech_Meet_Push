package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 9. 16.
 *
 *   서버에서 에러 발생 시 내려오는 메시지 공통 규격.
 */
data class ErrorResponse(

    @SerializedName("code")
    val code: String,

    @SerializedName("codeName")
    val codeName: String,

    @SerializedName("message")
    val message: String
)
