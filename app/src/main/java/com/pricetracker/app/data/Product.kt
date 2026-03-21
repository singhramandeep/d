package com.pricetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val description: String = "",
    val imageUrl: String = "",
    val currentPrice: Double,
    val originalPrice: Double? = null,
    val currency: String = "₹",
    val source: String, // flipkart, amazon, myntra
    val createdAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long = System.currentTimeMillis(),
)
