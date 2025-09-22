package matrix.push.sample.presentation.screen.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import matrix.push.sample.presentation.shopping.viewmodel.CatalogViewModel

/**
 * 상품 상세: productId로 상품 조회 후 장바구니 담기 제공.
 */
@Composable
fun ProductDetailScreen(productId: String) {
    val vm: CatalogViewModel = viewModel()
    val product = remember(productId) { vm.findProduct(productId) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 이미지 섹션
        AsyncImage(
            model = product?.imageUrl,
            contentDescription = product?.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )

        // 타이틀/가격
        Text(text = product?.name ?: "Unknown", style = MaterialTheme.typography.headlineSmall)
        Text(text = product?.price?.toString()?.plus("원") ?: "-", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

        // CTA 버튼
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(modifier = Modifier.weight(1f), onClick = { vm.addToCart(productId) }) { Text("장바구니") }
            Button(modifier = Modifier.weight(1f), onClick = { /* 구매하기 */ }) { Text("구매하기") }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // About this item
        Text(text = "About this item", style = MaterialTheme.typography.titleMedium)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Bullet(text = "High quality materials with durable design.")
                Bullet(text = "Ergonomic fit for everyday use.")
                Bullet(text = "Compatible with popular devices.")
                Bullet(text = "1-year limited warranty.")
            }
        }

        // Specifications
        Text(text = "Specifications", style = MaterialTheme.typography.titleMedium)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecRow(label = "Brand", value = "Matrix")
                SpecRow(label = "Model", value = product?.id ?: "-")
                SpecRow(label = "Category", value = "See top")
                SpecRow(label = "Package", value = "Standard")
            }
        }

        // Delivery/Returns
        Text(text = "Delivery & Returns", style = MaterialTheme.typography.titleMedium)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Free standard shipping over 50,000원")
                Text(text = "30-day return policy")
                Text(text = "Ships within 2 business days")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun Bullet(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "• ", modifier = Modifier.padding(end = 4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}


