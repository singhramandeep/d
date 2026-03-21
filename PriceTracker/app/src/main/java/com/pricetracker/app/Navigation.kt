package com.pricetracker.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pricetracker.app.presentation.screens.add.AddProductScreen
import com.pricetracker.app.presentation.screens.detail.ProductDetailScreen
import com.pricetracker.app.presentation.screens.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddProduct : Screen("add_product?url={url}") {
        fun createRoute(url: String = "") = if (url.isEmpty()) "add_product" else "add_product?url=${url}"
    }
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Long) = "product/$productId"
    }
}

@Composable
fun PriceTrackerNavHost(
    navController: NavHostController,
    sharedUrl: String?
) {
    NavHost(
        navController = navController,
        startDestination = if (!sharedUrl.isNullOrEmpty()) Screen.AddProduct.route else Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onAddProduct = {
                    navController.navigate(Screen.AddProduct.createRoute())
                }
            )
        }

        composable(
            route = "add_product?url={url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val urlArg = backStackEntry.arguments?.getString("url")
            val effectiveUrl = urlArg?.takeIf { it.isNotEmpty() } ?: sharedUrl

            AddProductScreen(
                sharedUrl = effectiveUrl,
                onNavigateBack = { navController.popBackStack() },
                onProductAdded = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.LongType }
            )
        ) {
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
