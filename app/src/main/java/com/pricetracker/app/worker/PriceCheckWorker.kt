package com.pricetracker.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pricetracker.app.data.repository.ProductRepository
import com.pricetracker.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PriceCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ProductRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("PriceCheckWorker", "Starting price check for all products")
        return try {
            val products = repository.getAllProductsList()
            for (product in products) {
                try {
                    val result = repository.refreshPrice(product.id)
                    result.onSuccess { update ->
                        if (update.changed) {
                            NotificationHelper.sendPriceChangeNotification(
                                applicationContext,
                                update.product,
                                update.oldPrice,
                                update.newPrice
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PriceCheckWorker", "Error checking ${product.name}", e)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("PriceCheckWorker", "Worker failed", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "price_check_worker"
    }
}
