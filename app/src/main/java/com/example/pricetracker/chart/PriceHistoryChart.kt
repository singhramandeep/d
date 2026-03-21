package com.example.pricetracker.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.pricetracker.data.PriceHistoryEntity

@Composable
fun PriceHistoryChart(
    history: List<PriceHistoryEntity>,
    modifier: Modifier = Modifier
) {
    if (history.size < 2) {
        Box(modifier = modifier.padding(16.dp)) {
            Text("Not enough points yet to render chart.")
        }
        return
    }
    val lineColor = MaterialTheme.colorScheme.primary
    val points = buildScaledPoints(history)
    Canvas(modifier = modifier.fillMaxSize().padding(8.dp)) {
        val path = Path()
        val first = points.first()
        path.moveTo(first.x * size.width, first.y * size.height)
        points.drop(1).forEach { point ->
            path.lineTo(point.x * size.width, point.y * size.height)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f)
        )
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = Offset(point.x * size.width, point.y * size.height)
            )
        }
    }
}

private fun buildScaledPoints(history: List<PriceHistoryEntity>): List<Offset> {
    val prices = history.map { it.price }
    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 1.0
    val spread = (maxPrice - minPrice).takeIf { it > 0 } ?: 1.0
    val lastIndex = (history.size - 1).coerceAtLeast(1)
    return history.mapIndexed { index, entry ->
        val x = index.toFloat() / lastIndex.toFloat()
        val normalizedY = ((entry.price - minPrice) / spread).toFloat()
        Offset(x, 1f - normalizedY)
    }
}
