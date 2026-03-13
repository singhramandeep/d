package com.example.pricetracker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    state: MainUiState,
    onRefresh: () -> Unit,
    onTrackPending: (String) -> Unit,
    onDismissPending: () -> Unit,
    onUpdateTarget: (Long, String) -> Unit,
    onRemoveProduct: (Long) -> Unit,
    onClearMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Header(
            productCount = state.products.size,
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh
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
                        onUpdateTarget = onUpdateTarget,
                        onRemove = onRemoveProduct
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    productCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("PricePulse", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "$productCount tracked products",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(onClick = onRefresh, enabled = !isRefreshing) {
            Text(if (isRefreshing) "Refreshing..." else "Refresh")
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
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(message, modifier = Modifier.weight(1f))
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
    onUpdateTarget: (Long, String) -> Unit,
    onRemove: (Long) -> Unit
) {
    val context = LocalContext.current
    var targetText by remember(product.id, product.targetPricePaise) {
        mutableStateOf(product.targetPricePaise?.let { "%.2f".format(it / 100.0) } ?: "")
    }
    val historyText = product.history.takeLast(8).joinToString(" → ") { "₹%.2f".format(it / 100.0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(product.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(product.source, style = MaterialTheme.typography.labelMedium)
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.url))
                    context.startActivity(intent)
                }
            ) {
                Text("Open product link")
            }
            Text("Current price: ${product.currentPricePaise?.let { inr(it) } ?: "N/A"}")
            Text("Target price: ${product.targetPricePaise?.let { inr(it) } ?: "Not set"}")
            Text("Last checked: ${formatTime(product.lastCheckedAt)}")
            if (historyText.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                Text("Price history", fontWeight = FontWeight.Medium)
                Text(historyText, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it },
                label = { Text("Update target (INR)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onUpdateTarget(product.id, targetText) }) {
                    Text("Save target")
                }
                TextButton(onClick = { onRemove(product.id) }) {
                    Text("Remove")
                }
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
