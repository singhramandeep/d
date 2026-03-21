package com.example.pricetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pricetracker.AppContainer
import com.example.pricetracker.chart.PriceHistoryChart
import com.example.pricetracker.domain.HistoryRange
import com.example.pricetracker.ui.components.PollingIntervalSelector
import com.example.pricetracker.ui.components.ProductCard
import com.example.pricetracker.utils.Formatters
import kotlinx.coroutines.flow.Flow

@Composable
fun PriceTrackerRoot(
    container: AppContainer,
    sharedUrls: Flow<String>,
    initialProductId: Long?
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(container.repository, container.settingsRepository)
    )
    val mainState by mainViewModel.uiState.collectAsState()

    HandleRootSideEffects(
        sharedUrls = sharedUrls,
        initialProductId = initialProductId,
        mainState = mainState,
        mainViewModel = mainViewModel,
        navController = navController,
        snackbarHostState = snackbarHostState
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AppNavHost(
            padding = padding,
            container = container,
            navController = navController,
            state = mainState,
            mainViewModel = mainViewModel
        )
    }
}

@Composable
private fun HandleRootSideEffects(
    sharedUrls: Flow<String>,
    initialProductId: Long?,
    mainState: MainUiState,
    mainViewModel: MainViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(sharedUrls) {
        sharedUrls.collect { url -> mainViewModel.addProduct(url) }
    }
    LaunchedEffect(mainState.message) {
        val text = mainState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        mainViewModel.consumeMessage()
    }
    LaunchedEffect(initialProductId) {
        val id = initialProductId ?: return@LaunchedEffect
        navController.navigate("detail/$id")
    }
}

@Composable
private fun AppNavHost(
    padding: PaddingValues,
    container: AppContainer,
    navController: NavHostController,
    state: MainUiState,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(padding)
    ) {
        composable("home") {
            HomeScreen(
                state = state,
                onUrlChanged = mainViewModel::onUrlChanged,
                onAddClick = { mainViewModel.addProduct() },
                onRefresh = mainViewModel::refreshProduct,
                onDelete = mainViewModel::deleteProduct,
                onOpenProduct = { id -> navController.navigate("detail/$id") },
                onIntervalSelected = mainViewModel::setPollingInterval
            )
        }
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: return@composable
            val detailViewModel: ProductDetailViewModel = viewModel(
                key = "detail-$productId",
                factory = ProductDetailViewModel.Factory(productId, container.repository)
            )
            val detailState by detailViewModel.uiState.collectAsState()
            DetailScreen(detailState, detailViewModel::selectRange)
        }
    }
}

@Composable
private fun HomeScreen(
    state: MainUiState,
    onUrlChanged: (String) -> Unit,
    onAddClick: () -> Unit,
    onRefresh: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenProduct: (Long) -> Unit,
    onIntervalSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.urlInput,
                onValueChange = onUrlChanged,
                label = { Text("Product URL") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onAddClick, enabled = !state.isLoading) {
                Text(if (state.isLoading) "Adding..." else "Add")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Polling interval", style = MaterialTheme.typography.titleSmall)
        PollingIntervalSelector(
            current = state.pollingIntervalMinutes,
            options = listOf(1, 5, 10, 15),
            onSelect = onIntervalSelected
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(state.products, key = { item -> item.id }) { product ->
                ProductCard(
                    product = product,
                    onRefresh = { onRefresh(product.id) },
                    onDelete = { onDelete(product.id) },
                    onOpen = { onOpenProduct(product.id) }
                )
            }
        }
    }
}

@Composable
private fun DetailScreen(
    state: ProductDetailUiState,
    onRangeSelected: (HistoryRange) -> Unit
) {
    if (state.product == null) {
        Text("Product not found", modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ProductHeader(state) }
        item { RangeSelector(state.range, onRangeSelected) }
        item {
            PriceHistoryChart(
                history = state.history,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }
        item { Text("Price history", style = MaterialTheme.typography.titleMedium) }
        items(state.history.reversed(), key = { it.id }) { point ->
            HistoryListItem(point.price, point.timestamp)
        }
    }
}

@Composable
private fun ProductHeader(state: ProductDetailUiState) {
    val product = state.product ?: return
    AsyncImage(
        model = product.imageUrl,
        contentDescription = product.title,
        modifier = Modifier.fillMaxWidth().height(220.dp),
        contentScale = ContentScale.Crop
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(product.title, style = MaterialTheme.typography.headlineSmall)
    Text("Current: ${Formatters.price(product.currentPrice)}")
    Text("Last checked: ${Formatters.dateTime(product.lastCheckedAt)}")
}

@Composable
private fun RangeSelector(range: HistoryRange, onRangeSelected: (HistoryRange) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryRange.entries.forEach { item ->
            FilterChip(
                selected = range == item,
                onClick = { onRangeSelected(item) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun HistoryListItem(price: Double, timestamp: Long) {
    Column {
        Text(Formatters.price(price), style = MaterialTheme.typography.bodyLarge)
        Text(Formatters.dateTime(timestamp), style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider()
    }
}
