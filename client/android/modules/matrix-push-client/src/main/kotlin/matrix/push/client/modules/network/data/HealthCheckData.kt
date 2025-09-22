package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 */

// 서버가 "HEALTH_CHECK" 이벤트 시 data 필드에 보내주는 JSON 구조
data class HealthCheckEventData(

    @SerializedName("healthCheckId")
    val healthCheckId: String
)

// 서버의 Health Check API 가 받은 요청 body.
data class HealthCheckRequest (

    @SerializedName("matrixPushId")
    val matrixPushId: String,

    @SerializedName("healthCheckId")
    val healthCheckId: String,
)
