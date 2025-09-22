package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 31.
 *
 *   로그아웃 요청 규격.
 */
data class LogoutRequest(

    // 필수
    @SerializedName("matrixPushId")
    val matrixPushId: String,

    // 필수
    @SerializedName("appIdentifier")
    val appIdentifier: String,

    // 필수
    @SerializedName("deviceId")
    val deviceId: String,

    // 필수
    @SerializedName("platform")
    val platform: String

)