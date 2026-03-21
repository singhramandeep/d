package com.pricetracker.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val currentPrice: Double,
    val originalPrice: Double,
    val currency: String = "₹",
    val platform: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isTracking: Boolean = true
)
