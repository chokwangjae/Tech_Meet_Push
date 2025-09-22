package matrix.push.sample.presentation.screen.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
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
 * 홈 화면: 카테고리 요약 + 추천 상품 그리드(간단 리스트) 노출.
 */
@Composable
fun HomeScreen(onProductClick: (String) -> Unit = {}) {
    val vm: CatalogViewModel = viewModel()
    val categories by vm.categories.collectAsState()
    val products by vm.products.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Deals for you (Home)", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Home chip: 전체 보기
            CategoryChip(category = matrix.push.sample.presentation.shopping.data.Category("all", "Home")) { vm.loadHome() }
            categories.forEach { c ->
                CategoryChip(category = c) { vm.loadCategory(c.id) }
            }
        }

        Text(text = "Recommended", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                items(products) { p ->
                    ProductCard(
                        product = p,
                        onClick = { onProductClick(p.id) },
                        onAdd = { vm.addToCart(p.id) }
                    )
                }
            }
        )
    }
}


