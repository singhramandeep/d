package com.pricetracker.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "price_history",
    primaryKeys = ["productId", "timestamp"],
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class PriceHistory(
    val productId: Long,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
)
