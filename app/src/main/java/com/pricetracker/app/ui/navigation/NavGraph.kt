package com.pricetracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pricetracker.app.ui.screens.addproduct.AddProductScreen
import com.pricetracker.app.ui.screens.detail.DetailScreen
import com.pricetracker.app.ui.screens.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_PRODUCT = "add_product"
    const val ADD_PRODUCT_WITH_URL = "add_product?url={url}"
    const val PRODUCT_DETAIL = "product/{productId}"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    sharedUrl: String? = null
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddProduct = {
                    navController.navigate(Routes.ADD_PRODUCT)
                },
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
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
            val url = backStackEntry.arguments?.getString("url")
                ?.takeIf { it.isNotBlank() }
                ?: sharedUrl
            AddProductScreen(
                initialUrl = url,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "product/{productId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.LongType }
            )
        ) {
            DetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
