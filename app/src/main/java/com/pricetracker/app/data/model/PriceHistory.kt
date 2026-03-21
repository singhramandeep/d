package com.pricetracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
)
