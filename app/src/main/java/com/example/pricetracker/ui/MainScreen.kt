package com.example.pricetracker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeaderSummary(
            productCount = state.products.size,
            isRefreshing = state.isRefreshingAll,
            onRefresh = onRefreshAll,
            onCopyAll = {
                clipboardManager.setText(AnnotatedString(buildAllItemsClipboardText(state.products)))
                onAllItemsCopied(state.products.size)
            }
        )
        Spacer(Modifier.height(12.dp))

        state.pendingSharedUrl?.let { pendingUrl ->
            PendingShareCard(
                pendingUrl = pendingUrl,
                onTrack = onTrackPending,
                onDismiss = onDismissPending
            )
            Spacer(Modifier.height(12.dp))
        }

        state.message?.let { message ->
            InfoMessage(message = message, onDismiss = onClearMessage)
            Spacer(Modifier.height(12.dp))
        }

        if (state.products.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    onRefresh: () -> Unit,
    onCopyAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column {
            Text("PricePulse", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("$productCount items", style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh, enabled = !isRefreshing) {
                Text(if (isRefreshing) "Checking all..." else "Check all prices")
            }
            TextButton(onClick = onCopyAll, enabled = productCount > 0) {
                Text("Copy all items")
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
    Card(
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
    Card(
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
    Card(
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
    var isExpanded by rememberSaveable(product.id) { mutableStateOf(false) }
    var targetText by remember(product.id, product.targetPricePaise) {
        mutableStateOf(product.targetPricePaise?.let { "%.2f".format(it / 100.0) } ?: "")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    maxLines = if (isExpanded) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Collapse" else "Expand")
                }
            }
            if (isExpanded) {
                ProductImage(imageUrl = product.imageUrl)
                Text(product.source, style = MaterialTheme.typography.labelMedium)
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
                        Text(if (isRefreshing) "Checking..." else "Check price now")
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.url))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Open")
                    }
                }
                PriceHistoryPlaceholder(product.history)
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target price (INR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { onUpdateTarget(product.id, targetText) },
                        label = { Text("Save target") }
                    )
                    TextButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, product.url)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share product link"))
                        }
                    ) {
                        Text("Share link")
                    }
                    TextButton(onClick = { onRemove(product.id) }) {
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
    AsyncImage(
        model = imageUrl,
        contentDescription = "Product image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Composable
private fun PriceHistoryPlaceholder(history: List<Long>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Price history", fontWeight = FontWeight.Medium)
            Text("Chart placeholder (coming soon)", style = MaterialTheme.typography.bodySmall)
            val recent = history.takeLast(6)
            if (recent.isNotEmpty()) {
                Text(
                    "Recent: ${recent.joinToString(" • ") { inr(it) }}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("No history yet.", style = MaterialTheme.typography.bodySmall)
            }
        }
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
        "${index + 1}. ${product.title}\n${product.url}"
    }.joinToString(separator = "\n\n")
}
