package matrix.push.sample.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 */
enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    Home(
        route = "home",
        title = "홈",
        icon = Icons.Filled.Home
    ),
    Cart(
        route = "cart",
        title = "장바구니",
        icon = Icons.Filled.ShoppingCart
    ),
    Notifications(
        route = "notifications",
        title = "알림",
        icon = Icons.AutoMirrored.Filled.Message
    ),
    Settings(
        route = "settings",
        title = "설정",
        icon = Icons.Filled.Settings
    );

    companion object {
        const val PRODUCT_DETAIL = "productDetail"
        const val PRODUCT_ID_ARG = "productId"
        val PRODUCT_DETAIL_ROUTE = "$PRODUCT_DETAIL/{$PRODUCT_ID_ARG}"
        fun productDetailRoute(productId: String): String = "$PRODUCT_DETAIL/$productId"
    }
}