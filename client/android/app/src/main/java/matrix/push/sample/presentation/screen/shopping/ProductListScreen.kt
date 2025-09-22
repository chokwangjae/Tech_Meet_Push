package matrix.push.sample.presentation.screen.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import matrix.push.sample.presentation.shopping.viewmodel.CatalogViewModel
import matrix.push.sample.presentation.shopping.ui.CategoryChip
import matrix.push.sample.presentation.shopping.ui.ProductCard

/**
 * 카테고리 화면: 선택된 카테고리의 상품 리스트.
 */
@Composable
fun ProductListScreen(onProductClick: (String) -> Unit) {
    val vm: CatalogViewModel = viewModel()
    val products by vm.products.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 간단히 모든 카테고리를 표시하여 전환
            vm.categories.collectAsState().value.forEach { c ->
                CategoryChip(category = c) { vm.loadCategory(c.id) }
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { p ->
                ProductCard(
                    product = p,
                    onClick = { onProductClick(p.id) },
                    onAdd = { vm.addToCart(p.id) }
                )
            }
        }
    }
}


