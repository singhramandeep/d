package com.pricetracker.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pricetracker.app.data.AppDatabase
import com.pricetracker.app.data.ProductRepository
import com.pricetracker.app.notifications.NotificationHelper

class PriceCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val repository = ProductRepository(db.productDao())

            val changedIds = repository.refreshAllProducts()

            if (changedIds.isNotEmpty()) {
                for (productId in changedIds) {
                    repository.getProductById(productId)?.let { product ->
                        NotificationHelper.showPriceDropNotification(
                            applicationContext,
                            product
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Price check failed", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "PriceCheckWorker"
    }
}
