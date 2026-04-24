package com.pricetracker.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pricetracker.app.data.AppDatabase
import com.pricetracker.app.data.ProductRepository
import com.pricetracker.app.ui.screens.AddProductScreen
import com.pricetracker.app.ui.screens.ProductDetailScreen
import com.pricetracker.app.ui.screens.ProductListScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PriceTrackerApp(
    sharedUrl: String? = null,
    onClearSharedUrl: () -> Unit = {}
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
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 3.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge
                )
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
