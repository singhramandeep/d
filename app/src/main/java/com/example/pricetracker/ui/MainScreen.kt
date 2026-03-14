package com.example.pricetracker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUiState,
    onRefreshAll: () -> Unit,
    onTrackPending: (String) -> Unit,
    onDismissPending: () -> Unit,
    onCheckProductNow: (Long) -> Unit,
    onUpdateTarget: (Long, String) -> Unit,
    onRemoveProduct: (Long) -> Unit,
    onAllItemsCopied: (Int) -> Unit,
    onClearMessage: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PricePulse", style = MaterialTheme.typography.titleLarge)
                        Text("${state.products.size} items tracked", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(
                                AnnotatedString(buildAllItemsClipboardText(state.products))
                            )
                            onAllItemsCopied(state.products.size)
                        },
                        enabled = state.products.isNotEmpty()
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy all tracked items")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeaderSummary(
                    productCount = state.products.size,
                    isRefreshing = state.isRefreshingAll,
                    onRefresh = onRefreshAll
                )
            }

            state.pendingSharedUrl?.let { pendingUrl ->
                item {
                    PendingShareCard(
                        pendingUrl = pendingUrl,
                        onTrack = onTrackPending,
                        onDismiss = onDismissPending
                    )
                }
            }

            state.message?.let { message ->
                item {
                    InfoMessage(message = message, onDismiss = onClearMessage)
                }
            }

            if (state.products.isEmpty()) {
                item { EmptyState() }
            } else {
                items(items = state.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isRefreshing = state.refreshingProductIds.contains(product.id),
                        onCheckNow = onCheckProductNow,
                        onUpdateTarget = onUpdateTarget,
                        onRemove = onRemoveProduct
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSummary(
    productCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$productCount items in watchlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Track price drops across Amazon, Flipkart and more.",
                style = MaterialTheme.typography.bodySmall
            )
            FilledTonalButton(onClick = onRefresh, enabled = !isRefreshing) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isRefreshing) "Checking all prices..." else "Check all prices now")
            }
            if (isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PendingShareCard(
    pendingUrl: String,
    onTrack: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var thresholdText by remember { mutableStateOf("") }
    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Shared link detected", fontWeight = FontWeight.SemiBold)
            Text(pendingUrl, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = thresholdText,
                onValueChange = { thresholdText = it },
                label = { Text("Target price (INR)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onTrack(thresholdText) }) {
                    Text("Track")
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun InfoMessage(
    message: String,
    onDismiss: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(message)
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = "Share an Amazon/Flipkart product link to this app from any app and start tracking.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProductCard(
    product: ProductUiModel,
    isRefreshing: Boolean,
    onCheckNow: (Long) -> Unit,
    onUpdateTarget: (Long, String) -> Unit,
    onRemove: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by rememberSaveable(product.id) { mutableStateOf(false) }
    var targetText by remember(product.id, product.targetPricePaise) {
        mutableStateOf(product.targetPricePaise?.let { "%.2f".format(it / 100.0) } ?: "")
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    product.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (isExpanded) 4 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    val icon = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore
                    Icon(icon, contentDescription = if (isExpanded) "Collapse" else "Expand")
                }
            }
            if (isExpanded) {
                ProductImage(imageUrl = product.imageUrl)
                Text(product.source, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Last checked price: ${product.currentPricePaise?.let { inr(it) } ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Checked ${formatRelativeTime(product.lastCheckedAt)} (${formatTime(product.lastCheckedAt)})",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onCheckNow(product.id) }, enabled = !isRefreshing) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isRefreshing) "Checking..." else "Check price now")
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.url))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Rounded.Link, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Open")
                    }
                }
                PriceHistorySection(product.history)
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target price (INR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onUpdateTarget(product.id, targetText) }) {
                        Text("Save target")
                    }
                    TextButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, product.url)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share product link"))
                        }
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Share link")
                    }
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(product.url))
                        }
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                    }
                    TextButton(onClick = { onRemove(product.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductImage(imageUrl: String?) {
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("No product image yet")
        }
        return
    }
    Surface(shape = RoundedCornerShape(8.dp)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Product image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
    }
}

@Composable
private fun PriceHistorySection(history: List<PricePointUiModel>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Price history", fontWeight = FontWeight.Medium)
            if (history.isEmpty()) {
                Text("No history yet. Tap “Check price now” to fetch the first point.")
            } else {
                Sparkline(history = history)
                history.takeLast(4).reversed().forEach { point ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(inr(point.pricePaise), style = MaterialTheme.typography.bodySmall)
                        Text(formatRelativeTime(point.checkedAt), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkline(history: List<PricePointUiModel>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val prices = history.map { it.pricePaise.toFloat() }
    val minPrice = prices.minOrNull() ?: return
    val maxPrice = prices.maxOrNull() ?: return
    val range = (maxPrice - minPrice).takeIf { it > 0f } ?: 1f

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp)
    ) {
        if (prices.size == 1) {
            drawLine(
                color = primaryColor.copy(alpha = 0.35f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 3f
            )
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            return@Canvas
        }

        val spacing = size.width / (prices.size - 1).coerceAtLeast(1)
        val path = Path()
        prices.forEachIndexed { index, price ->
            val x = spacing * index
            val normalized = (price - minPrice) / range
            val y = size.height - (normalized * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawRect(
            color = primaryColor.copy(alpha = 0.08f),
            size = Size(size.width, size.height)
        )
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}

private fun inr(paise: Long): String = "₹%.2f".format(paise / 100.0)

private fun formatTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun formatRelativeTime(epochMillis: Long): String {
    val duration = Duration.between(
        Instant.ofEpochMilli(epochMillis),
        Instant.now()
    )
    if (duration.isNegative) return "just now"

    val days = duration.toDays()
    if (days > 0) return "$days day${if (days == 1L) "" else "s"} ago"

    val hours = duration.toHours()
    if (hours > 0) return "$hours hour${if (hours == 1L) "" else "s"} ago"

    val minutes = duration.toMinutes()
    if (minutes > 0) return "$minutes minute${if (minutes == 1L) "" else "s"} ago"

    return "just now"
}

private fun buildAllItemsClipboardText(products: List<ProductUiModel>): String {
    return products.mapIndexed { index, product ->
        "${index + 1}. ${product.title}\n${product.url}\nPrice: ${product.currentPricePaise?.let { inr(it) } ?: "N/A"}"
    }.joinToString(separator = "\n\n")
}
