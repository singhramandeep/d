package com.example.pricetracker.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pricetracker.AppContainer
import com.example.pricetracker.notifications.NotificationHelper
import java.util.concurrent.TimeUnit

class PriceCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = AppContainer.repository(applicationContext)
            repo.refreshAllPrices { product, currentPrice ->
                val target = product.targetPricePaise ?: return@refreshAllPrices
                NotificationHelper.notifyThresholdReached(
                    context = applicationContext,
                    productId = product.id,
                    title = product.title,
                    currentPricePaise = currentPrice,
                    targetPricePaise = target
                )
            }
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "price-check-worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PriceCheckWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
