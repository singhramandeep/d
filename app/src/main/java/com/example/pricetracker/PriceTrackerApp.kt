package com.example.pricetracker

import android.app.Application
import androidx.work.Configuration
import com.example.pricetracker.worker.WorkerFactoryProvider

class PriceTrackerApp : Application(), Configuration.Provider {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.createChannel()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(WorkerFactoryProvider(container))
            .build()
}
