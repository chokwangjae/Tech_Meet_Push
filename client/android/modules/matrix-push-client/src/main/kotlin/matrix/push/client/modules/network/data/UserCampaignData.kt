package matrix.push.client.modules.network.data

import com.google.gson.annotations.SerializedName

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 */

/**
 *  사용자가 속해있는 캠페인 목록을 가져온다. (Request)
 */
data class UserCampaignGetRequest(

    @SerializedName("matrixPushId")
    val matrixPushId: String,
)


/**
 * 사용자가 속해있는 캠페인 목록을 가져온다. (Response)
 */
data class UserCampaignGetResponse(
    @SerializedName("campaignId")
    val campaignId: Long,

    @SerializedName("campaignName")
    val campaignName: String,

    @SerializedName("consented")
    val consented: Boolean
)