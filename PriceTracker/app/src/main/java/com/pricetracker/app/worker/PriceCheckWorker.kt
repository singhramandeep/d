package com.pricetracker.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pricetracker.app.MainActivity
import com.pricetracker.app.R
import com.pricetracker.app.data.repository.ProductRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PriceCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ProductRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "price_check_worker"
        private const val TAG = "PriceCheckWorker"
        private const val CHANNEL_ID = "price_alerts"
        private const val CHANNEL_NAME = "Price Alerts"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting price check for all products")
            createNotificationChannel()

            val priceDrops = repository.refreshAllActiveProducts()

            priceDrops.forEach { (product, previousPrice) ->
                sendPriceDropNotification(product.id, product.name, previousPrice, product.currentPrice, product.currency)
                Log.d(TAG, "Price drop: ${product.name} from ${product.currency}${previousPrice} to ${product.currency}${product.currentPrice}")
            }

            Log.d(TAG, "Price check complete. ${priceDrops.size} price drops found.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Price check failed: ${e.message}", e)
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when tracked product prices drop"
            }
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendPriceDropNotification(
        productId: Long,
        productName: String,
        previousPrice: Double,
        newPrice: Double,
        currency: String
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("product_id", productId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            productId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val discount = ((previousPrice - newPrice) / previousPrice * 100).toInt()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Price Drop Alert! $discount% Off")
            .setContentText("$productName dropped to $currency${String.format("%.0f", newPrice)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$productName is now $currency${String.format("%.0f", newPrice)} (was $currency${String.format("%.0f", previousPrice)}). Save $discount%!")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((NOTIFICATION_ID_BASE + productId).toInt(), notification)
    }
}
