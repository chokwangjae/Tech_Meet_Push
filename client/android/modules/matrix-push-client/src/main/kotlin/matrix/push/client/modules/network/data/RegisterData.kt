package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 */

// 기기 등록 및 푸시 토큰 업데이트 시 사용할 data.
data class RegisterRequest(

    @SerializedName("appIdentifier")
    val appIdentifier: String,

    @SerializedName("deviceId")
    val deviceId: String,

    @SerializedName("platform")
    val platform: String,

    @SerializedName("pushToken")
    val pushToken: String
)

// 기기 등록 및 푸시 토큰 업데이트 시 Response 에 사용할 data.
data class RegisterResponse(
    @SerializedName("pushMode")
    val pushMode: String,
    @SerializedName("rId")
    val rId: String
)
