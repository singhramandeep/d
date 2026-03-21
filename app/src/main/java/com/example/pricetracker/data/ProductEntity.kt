package com.example.pricetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val source: String,
    val title: String,
    val imageUrl: String,
    val currentPrice: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long = System.currentTimeMillis()
)
