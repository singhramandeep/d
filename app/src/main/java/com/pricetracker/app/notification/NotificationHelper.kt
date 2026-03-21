package com.pricetracker.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pricetracker.app.MainActivity
import com.pricetracker.app.R
import com.pricetracker.app.data.model.Product

object NotificationHelper {

    private const val CHANNEL_ID = "price_alerts"
    private const val CHANNEL_NAME = "Price Alerts"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun sendPriceChangeNotification(
        context: Context,
        product: Product,
        oldPrice: Double,
        newPrice: Double
    ) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("product_id", product.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, product.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priceDropped = newPrice < oldPrice
        val title = if (priceDropped) "📉 Price Dropped!" else "📈 Price Changed"
        val diff = if (priceDropped) oldPrice - newPrice else newPrice - oldPrice
        val direction = if (priceDropped) "dropped" else "increased"
        val body = "${product.name}\n${product.currency}${formatPrice(oldPrice)} → ${product.currency}${formatPrice(newPrice)} ($direction by ${product.currency}${formatPrice(diff)})"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText("${product.name}: ${product.currency}${formatPrice(newPrice)}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (priceDropped) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(product.id.toInt(), notification)
    }

    private fun formatPrice(price: Double): String {
        return if (price == price.toLong().toDouble()) {
            "%,.0f".format(price)
        } else {
            "%,.2f".format(price)
        }
    }
}
