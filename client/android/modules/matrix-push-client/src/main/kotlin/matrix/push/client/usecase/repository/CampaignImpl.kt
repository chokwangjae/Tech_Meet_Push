package matrix.push.client.usecase.repository

import matrix.commons.log.MatrixLog
import matrix.push.client.modules.error.MpsError
import matrix.push.client.modules.MatrixPushModules
import matrix.push.client.modules.error.MpsException
import matrix.push.client.modules.network.ApiExecutor
import matrix.push.client.modules.network.PushService
import matrix.push.client.modules.network.data.UserCampaignGetRequest
import matrix.push.client.modules.network.data.UserCampaignGetResponse
import matrix.push.client.modules.network.data.UserConsentCampaignRequest
import matrix.push.client.modules.network.data.UserConsentRequest
import matrix.push.client.modules.network.data.UserConsentResponse

/**
 *   @author tarkarn
 *   @since 2025. 7. 21.
 *
 *   캠페인 관련 구현체.
 */
internal class CampaignImpl(
    val pushService: PushService,
    val apiExecutor: ApiExecutor
): CampaignRepository {

    companion object {
        private const val TAG = "CampaignImpl"
    }

    override suspend fun fetchUserCampaigns(): List<UserCampaignGetResponse> {
        try {
            val response = apiExecutor.execute {
                val requestBody = UserCampaignGetRequest(
                    matrixPushId = MatrixPushModules.getMatrixPushId()
                )

                pushService.userCampaignList(requestBody)
            }

            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                MatrixLog.e(TAG, "Failed to get campaign list. Code: ${response.code()}, Message: $errorBody")
                return emptyList()
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to get campaign list. Unknown exception.", e)
            return emptyList()
        }
    }

    override suspend fun updateUserConsent(consented: Boolean): UserConsentResponse {
        try {
            val response = apiExecutor.execute {
                val requestBody = UserConsentRequest(
                    matrixPushId = MatrixPushModules.getMatrixPushId(),
                    consented = consented
                )

                pushService.updateUserConsent(requestBody)
            }

            if (response.isSuccessful) {
                // 성공 시 body를 반환, null이면 예외 발생
                return response.body() ?: throw MpsException(MpsError.CAMPAIGN_INFO_FAILED, "Response body is null after updating user consent")
            } else {
                throw MpsException(MpsError.CAMPAIGN_INFO_FAILED, "Failed to update user consent with code: ${response.code()}")
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to update user consent.", e)
            if (e is MpsException) {
                throw e
            } else {
                throw MpsException(MpsError.CAMPAIGN_INFO_FAILED, "Failed to update user consent", e)
            }
        }
    }

    override suspend fun updateCampaignConsent(campaignId: Long, consented: Boolean): UserConsentResponse {
        try {
            val response = apiExecutor.execute {
                val requestBody = UserConsentCampaignRequest(
                    matrixPushId = MatrixPushModules.getMatrixPushId(),
                    campaignId = campaignId,
                    consented = consented
                )

                pushService.updateCampaignConsent(requestBody)
            }

            if (response.isSuccessful) {
                // 성공 시 body를 반환, null이면 빈 리스트 반환
                return response.body() ?: throw MpsException(MpsError.CAMPAIGN_INFO_FAILED, "Response body is null after updating campaign consent")
            } else {
                throw MpsException(MpsError.CAMPAIGN_INFO_FAILED, "Failed to update campaign consent for $campaignId with code: ${response.code()}")
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to update campaign consent.", e)
            if (e is MpsException) {
                throw e
            } else {
                throw MpsException(MpsError.CAMPAIGN_INFO_FAILED, "Failed to update campaign consent for $campaignId", e)
            }
        }
    }

}