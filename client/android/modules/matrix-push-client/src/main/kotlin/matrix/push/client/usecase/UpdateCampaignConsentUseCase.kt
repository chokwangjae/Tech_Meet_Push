package matrix.push.client.usecase

import matrix.push.client.modules.network.data.UserConsentResponse
import matrix.push.client.usecase.repository.CampaignRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 21.
 */
class UpdateCampaignConsentUseCase(
    private val campaignRepository: CampaignRepository
) {
    suspend operator fun invoke(campaignId: Long, consented: Boolean): UserConsentResponse {
        return campaignRepository.updateCampaignConsent(campaignId, consented)
    }
}