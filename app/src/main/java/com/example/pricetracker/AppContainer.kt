package com.example.pricetracker

import android.content.Context
import androidx.room.Room
import com.example.pricetracker.data.AppDatabase
import com.example.pricetracker.data.ProductRepository
import com.example.pricetracker.network.ProductFetcher
import com.example.pricetracker.notifications.PriceNotificationHelper
import com.example.pricetracker.settings.PollingSettingsRepository

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "price_tracker.db"
    ).fallbackToDestructiveMigration().build()

    private val fetcher = ProductFetcher()
    val notificationHelper = PriceNotificationHelper(context)
    val settingsRepository = PollingSettingsRepository(context)
    val repository = ProductRepository(
        productDao = database.productDao(),
        historyDao = database.priceHistoryDao(),
        fetcher = fetcher,
        notifications = notificationHelper
    )
}
