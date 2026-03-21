package com.example.pricetracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pricetracker.data.ProductRepository

class PricePollingWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val repository: ProductRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return runCatching {
            repository.refreshAllProducts(sendNotifications = true)
            Result.success()
        }.getOrElse { error ->
            Log.e("PricePollingWorker", "Polling job failed", error)
            Result.retry()
        }
    }
}
