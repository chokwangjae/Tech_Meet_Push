package matrix.push.sample.presentation.screen.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 결제 화면 스켈레톤.
 */
@Composable
fun CheckoutScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Checkout", style = MaterialTheme.typography.headlineMedium)
        Text(text = "결제 요약 ...")
        Button(onClick = { /* 결제 처리 */ }) { Text("결제 완료") }
    }
}




