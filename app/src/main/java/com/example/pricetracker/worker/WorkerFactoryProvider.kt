package com.example.pricetracker.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.pricetracker.AppContainer

class WorkerFactoryProvider(
    private val container: AppContainer
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            PricePollingWorker::class.java.name -> {
                PricePollingWorker(appContext, workerParameters, container.repository)
            }
            else -> null
        }
    }
}
