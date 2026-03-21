package com.pricetracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val name: String,
    val description: String = "",
    val imageUrl: String = "",
    val currentPrice: Double,
    val lowestPrice: Double,
    val highestPrice: Double,
    val store: String,
    val currency: String = "₹",
    val addedAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long = System.currentTimeMillis()
)
