package com.pricetracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pricetracker.app.data.model.PriceHistory
import com.pricetracker.app.data.model.Product

@Database(
    entities = [Product::class, PriceHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun priceHistoryDao(): PriceHistoryDao
}
