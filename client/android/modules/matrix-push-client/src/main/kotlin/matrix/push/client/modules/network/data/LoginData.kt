package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 23.
 */

/**
 * 로그인 시 요청 규격.
 */
data class LoginRequest(

    // 필수 아님.
    @SerializedName("userId")
    val userId: String,

    // 필수 아님.
    @SerializedName("userName")
    val userName: String,

    // 필수 아님.
    @SerializedName("email")
    val email: String,

    // 필수
    @SerializedName("platform")
    val platform: String,

    // 필수
    @SerializedName("deviceId")
    val deviceId: String,

    // 필수
    @SerializedName("appIdentifier")
    val appIdentifier: String,

    // 필수
    @SerializedName("rId")
    val rId: String
)

/**
 * 로그인 시 응답 규격
 *
 * 푸시 서비스에 대한 정책을 서버에서 정의하고 싶은 경우 이쪽에 응답값을 추가하여 내리면 된다.
 */
data class LoginResponse(
    @SerializedName("matrixPushId")
    val matrixPushId: String
)