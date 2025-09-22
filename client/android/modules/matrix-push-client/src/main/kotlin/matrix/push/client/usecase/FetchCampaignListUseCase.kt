package matrix.push.client.usecase

import matrix.push.client.modules.network.data.UserCampaignGetResponse
import matrix.push.client.usecase.repository.CampaignRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 21.
 *
 *   사용자 기기에 할당되어 있는 캠페인 목록을 가져온다.
 */
class FetchCampaignListUseCase(
    private val campaignRepository: CampaignRepository
){
    suspend operator fun invoke(): List<UserCampaignGetResponse> {
        return campaignRepository.fetchUserCampaigns()
    }
}