package matrix.push.sample.presentation.shopping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import matrix.push.sample.presentation.shopping.data.Category
import matrix.push.sample.presentation.shopping.data.Product
import coil.compose.AsyncImage

@Composable
fun CategoryChip(category: Category, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(onClick = onClick),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x11000000), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AsyncImage(
            model = product.imageUrl.ifBlank { null },
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F3F5))
        )
        Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = "${product.price}원", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0F7A10))
        Text(
            text = "Add to Cart",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF232F3E))
                .clickable(onClick = onAdd)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            color = Color.White
        )
    }
}


