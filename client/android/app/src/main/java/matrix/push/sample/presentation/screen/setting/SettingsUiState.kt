package matrix.push.sample.presentation.screen.setting

import matrix.push.client.modules.network.data.UserCampaignGetResponse

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 */
data class SettingsUiState(
    val isUserConsented: Boolean = true,
    val campaigns: List<UserCampaignGetResponse> = emptyList(),
    val isLoading: Boolean = false
)