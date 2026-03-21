package com.pricetracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.pricetracker.app.data.model.PriceHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriceChart(
    priceHistory: List<PriceHistory>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
) {
    if (priceHistory.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Not enough data for chart.\nPrice history will appear after updates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    val prices = priceHistory.map { it.price }
    val minPrice = prices.min()
    val maxPrice = prices.max()
    val priceRange = if (maxPrice - minPrice > 0) maxPrice - minPrice else 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price History",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${priceHistory.size} data points",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val textColor = MaterialTheme.colorScheme.onSurfaceVariant
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val stepX = canvasWidth / (priceHistory.size - 1).coerceAtLeast(1)

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = canvasHeight * i / gridLines
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
                val priceLabel = maxPrice - (priceRange * i / gridLines)
                drawContext.canvas.nativeCanvas.drawText(
                    "₹${formatChartPrice(priceLabel)}",
                    -44.dp.toPx(),
                    y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 9.dp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }

            val path = Path()
            val fillPath = Path()
            priceHistory.forEachIndexed { index, record ->
                val x = index * stepX
                val normalizedY = ((maxPrice - record.price) / priceRange).toFloat()
                val y = normalizedY * canvasHeight

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, canvasHeight)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(canvasWidth, canvasHeight)
            fillPath.close()

            drawPath(fillPath, fillColor)
            drawPath(
                path, lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            priceHistory.forEachIndexed { index, record ->
                val x = index * stepX
                val normalizedY = ((maxPrice - record.price) / priceRange).toFloat()
                val y = normalizedY * canvasHeight
                drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
            }

            val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
            val labelCount = minOf(priceHistory.size, 5)
            val step = (priceHistory.size - 1) / (labelCount - 1).coerceAtLeast(1)
            for (i in 0 until labelCount) {
                val index = minOf(i * step, priceHistory.size - 1)
                val x = index * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    dateFormatter.format(Date(priceHistory[index].timestamp)),
                    x,
                    canvasHeight + 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 9.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

private fun formatChartPrice(price: Double): String {
    return when {
        price >= 100000 -> "${(price / 100000).let { if (it == it.toLong().toDouble()) "${it.toLong()}L" else "%.1fL".format(it) }}"
        price >= 1000 -> "${(price / 1000).let { if (it == it.toLong().toDouble()) "${it.toLong()}K" else "%.1fK".format(it) }}"
        else -> "%.0f".format(price)
    }
}
