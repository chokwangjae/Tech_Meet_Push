package matrix.push.client.usecase

import matrix.push.client.modules.network.data.UserConsentResponse
import matrix.push.client.usecase.repository.CampaignRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 21.
 */
class UpdateUserConsentUseCase(private val campaignRepository: CampaignRepository) {
    suspend operator fun invoke(consented: Boolean): UserConsentResponse {
        return campaignRepository.updateUserConsent(consented)
    }
}