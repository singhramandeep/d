package com.pricetracker.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, PriceHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PriceTrackerDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun priceHistoryDao(): PriceHistoryDao
}
