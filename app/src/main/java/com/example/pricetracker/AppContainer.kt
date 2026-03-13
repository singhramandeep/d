package com.example.pricetracker

import android.content.Context
import androidx.room.Room
import com.example.pricetracker.data.PriceRepository
import com.example.pricetracker.data.local.AppDatabase
import com.example.pricetracker.data.scraper.ProductScraper

object AppContainer {
    @Volatile
    private var db: AppDatabase? = null

    @Volatile
    private var repository: PriceRepository? = null

    fun repository(context: Context): PriceRepository {
        return repository ?: synchronized(this) {
            repository ?: PriceRepository(
                database = database(context),
                scraper = ProductScraper()
            ).also { repository = it }
        }
    }

    private fun database(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "price_pulse.db"
            ).build().also { db = it }
        }
    }
}
