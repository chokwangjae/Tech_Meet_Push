package matrix.push.sample.presentation.screen.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import matrix.push.sample.presentation.MainViewModel

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 */
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val uiState by viewModel.settingsUiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("푸시 수신 설정", style = MaterialTheme.typography.titleLarge) // M3 스타일로 변경
                Spacer(modifier = Modifier.height(16.dp))
                SettingItem(
                    text = "전체 푸시 수신 동의",
                    isChecked = uiState.isUserConsented,
                    onCheckedChange = { viewModel.updateUserConsent(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // M3 스타일로 변경
                Text("캠페인별 설정", style = MaterialTheme.typography.titleLarge) // M3 스타일로 변경
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(uiState.campaigns, key = { it.campaignId }) { campaign ->
                SettingItem(
                    text = campaign.campaignName,
                    isChecked = campaign.consented,
                    onCheckedChange = { viewModel.updateCampaignConsent(campaign.campaignId, it) }
                )
            }
        }
    }
}

@Composable
fun SettingItem(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 16.sp)
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}
