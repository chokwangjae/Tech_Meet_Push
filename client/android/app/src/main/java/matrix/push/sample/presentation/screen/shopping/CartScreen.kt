package matrix.push.sample.presentation.screen.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import matrix.push.sample.presentation.shopping.viewmodel.CartViewModel

/**
 * 장바구니 화면.
 */
@Composable
fun CartScreen() {
    val vm: CartViewModel = viewModel()
    val items by vm.cart.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Cart", style = MaterialTheme.typography.headlineMedium)
        if (items.isEmpty()) {
            Text(text = "장바구니에 담긴 항목 없음")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items, key = { it.product.id }) { ci ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ci.product.name)
                            Text(text = "${ci.product.price}원 x ${ci.quantity}")
                        }
                        Button(onClick = { vm.remove(ci.product.id) }) { Text("삭제") }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "합계: ${vm.total()}원", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { /* 결제 이동 */ }) { Text("결제하기") }
            }
        }
    }
}


