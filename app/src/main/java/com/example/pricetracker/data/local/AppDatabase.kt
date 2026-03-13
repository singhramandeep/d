package com.example.pricetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrackedProductEntity::class, PriceHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun priceHistoryDao(): PriceHistoryDao
}
