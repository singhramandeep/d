package com.pricetracker.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pricetracker.app.notifications.NotificationHelper
import com.pricetracker.app.worker.PriceCheckWorker
import java.util.concurrent.TimeUnit

class PriceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        schedulePriceChecks()
    }

    private fun schedulePriceChecks() {
        val workRequest = PeriodicWorkRequestBuilder<PriceCheckWorker>(
            4, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
