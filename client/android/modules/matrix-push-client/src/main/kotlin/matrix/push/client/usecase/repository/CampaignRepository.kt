package matrix.push.client.usecase.repository

import matrix.push.client.modules.network.data.UserCampaignGetResponse
import matrix.push.client.modules.network.data.UserConsentResponse

/**
 *   @author tarkarn
 *   @since 2025. 7. 21.
 */
interface CampaignRepository {

    /**
     * 사용자의 캠페인 목록을 원격 서버에서 가져온다.
     * @param matrixPushId 사용자의 고유 ID
     * @return 성공 시 UserCampaignGetResponse, 실패 시 예외 발생
     */
    suspend fun fetchUserCampaigns(): List<UserCampaignGetResponse>

    suspend fun updateUserConsent(consented: Boolean): UserConsentResponse
    suspend fun updateCampaignConsent(campaignId: Long, consented: Boolean): UserConsentResponse
}