package com.example.pricetracker.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ProductWithHistory(
    @Embedded val product: TrackedProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val history: List<PriceHistoryEntity>
)
