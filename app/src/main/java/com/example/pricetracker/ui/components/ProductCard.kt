package com.example.pricetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pricetracker.data.ProductEntity
import com.example.pricetracker.utils.Formatters

@Composable
fun ProductCard(
    product: ProductEntity,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen() }) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                modifier = Modifier.size(84.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Price: ${Formatters.price(product.currentPrice)}")
                Text("Source: ${product.source}")
                Text("Updated: ${Formatters.dateTime(product.updatedAt)}")
            }
            Column {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
