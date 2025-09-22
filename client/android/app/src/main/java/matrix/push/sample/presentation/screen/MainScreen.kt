package matrix.push.sample.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import matrix.push.sample.presentation.MainViewModel
import matrix.push.sample.presentation.screen.message.MessageListScreen
import matrix.push.sample.presentation.screen.setting.SettingsScreen
import matrix.push.sample.presentation.screen.shopping.CartScreen
import matrix.push.sample.presentation.screen.shopping.HomeScreen
import matrix.push.sample.presentation.screen.shopping.ProductDetailScreen
import matrix.push.sample.presentation.screen.shopping.ProductListScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import matrix.push.sample.presentation.mvi.NavEffect

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 */


@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            AppBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            viewModel.navEffects.collectLatest { effect ->
                android.util.Log.d("MainScreen", "Received nav effect: $effect")
                when (effect) {
                    is NavEffect.ToProductDetail -> {
                        android.util.Log.d("MainScreen", "Navigating to product detail: ${effect.productId}")
                        navController.navigate(Screen.productDetailRoute(effect.productId))
                    }
                    NavEffect.ToNotifications -> {
                        android.util.Log.d("MainScreen", "Navigating to notifications")
                        navController.navigate(Screen.Notifications.route)
                    }
                    NavEffect.ToHome -> {
                        android.util.Log.d("MainScreen", "Navigating to home")
                        navController.navigate(Screen.Home.route)
                    }
                    NavEffect.None -> {
                        android.util.Log.d("MainScreen", "No navigation effect")
                    }
                }
            }
        }
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(Screen.Home, Screen.Cart, Screen.Notifications, Screen.Settings)

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = "shop://app/home" })
        ) { HomeScreen(onProductClick = { id -> navController.navigate(Screen.productDetailRoute(id)) }) }
        composable(
            route = Screen.Cart.route,
            deepLinks = listOf(navDeepLink { uriPattern = "shop://app/cart" })
        ) { CartScreen() }
        composable(
            route = Screen.Notifications.route,
            deepLinks = listOf(navDeepLink { uriPattern = "shop://app/notifications" })
        ) { MessageListScreen(viewModel) }
        composable(
            route = Screen.Settings.route,
            deepLinks = listOf(navDeepLink { uriPattern = "shop://app/settings" })
        ) { SettingsScreen(viewModel) }
        composable(
            route = Screen.PRODUCT_DETAIL_ROUTE,
            deepLinks = listOf(navDeepLink { uriPattern = "shop://app/productDetail/{${Screen.PRODUCT_ID_ARG}}" })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString(Screen.PRODUCT_ID_ARG) ?: ""
            ProductDetailScreen(productId = productId)
        }
    }
}