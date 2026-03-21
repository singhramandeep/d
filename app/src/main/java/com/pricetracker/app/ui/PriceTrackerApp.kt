package com.pricetracker.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pricetracker.app.data.AppDatabase
import com.pricetracker.app.data.ProductRepository
import com.pricetracker.app.ui.screens.AddProductScreen
import com.pricetracker.app.ui.screens.ProductDetailScreen
import com.pricetracker.app.ui.screens.ProductListScreen

@Composable
fun PriceTrackerApp(
    sharedUrl: String? = null,
    onClearSharedUrl: () -> Unit = {}  // Reserved for clearing share intent
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { ProductRepository(db.productDao()) }
    val navController = rememberNavController()

    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            val encoded = URLEncoder.encode(sharedUrl, StandardCharsets.UTF_8.toString())
            navController.navigate("add/$encoded") {
                popUpTo("list") { inclusive = false }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(padding)
        ) {
            composable("list") {
                ProductListScreen(
                    repository = repository,
                    onProductClick = { navController.navigate("detail/$it") },
                    onAddClick = { navController.navigate("add") }
                )
            }
            composable("add") {
                AddProductScreen(
                    repository = repository,
                    initialUrl = null,
                    onNavigateBack = { navController.popBackStack() },
                    onProductAdded = { navController.popBackStack() }
                )
            }
            composable("add/{sharedUrl}") { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("sharedUrl") ?: ""
                val url = try {
                    java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
                } catch (_: Exception) { "" }
                AddProductScreen(
                    repository = repository,
                    initialUrl = url,
                    onNavigateBack = { navController.popBackStack() },
                    onProductAdded = { navController.popBackStack() }
                )
            }
            composable("detail/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    repository = repository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
