package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 *
 *   푸시 수신 동의/미동의 여부 업데이트 시 사용 할 data.
 */

/**
 * 사용자 별 전체 푸시 수신 동의 여부 업데이트 (Request)
 */
data class UserConsentRequest(

    @SerializedName("matrixPushId")
    val matrixPushId: String,

    @SerializedName("consented")
    val consented: Boolean
)

/**
 * 사용자 별 전체 푸시 수신 동의 여부 응답 규격 (Response)
 */
data class UserConsentResponse(

    @SerializedName("consented")
    val consented: Boolean,

    @SerializedName("campaignDetails")
    val campaignDetails: List<UserCampaignGetResponse>
)


/**
 * 사용자 별 캠페인 푸시 수신 동의 여부 업데이트 (Request)
 */
data class UserConsentCampaignRequest(
    @SerializedName("matrixPushId")
    val matrixPushId: String,

    @SerializedName("campaignId")
    val campaignId: Long,

    @SerializedName("consented")
    val consented: Boolean
)