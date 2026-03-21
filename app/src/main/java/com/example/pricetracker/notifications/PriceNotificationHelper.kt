package com.example.pricetracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.pricetracker.MainActivity
import com.example.pricetracker.R

class PriceNotificationHelper(private val context: Context) {
    private val channelId = "price_alerts"
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            channelId,
            "Price alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for tracked product price drops"
        }
        manager.createNotificationChannel(channel)
    }

    fun notifyPriceDrop(productId: Long, productTitle: String, oldPrice: Double, newPrice: Double) {
        if (!hasNotificationPermission()) {
            Log.w("PriceNotifications", "POST_NOTIFICATIONS permission not granted; skipping alert")
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("product_id", productId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            productId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = "Price dropped from ₹${oldPrice.toInt()} to ₹${newPrice.toInt()}"
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(productTitle)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(productId.toInt(), notification)
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
