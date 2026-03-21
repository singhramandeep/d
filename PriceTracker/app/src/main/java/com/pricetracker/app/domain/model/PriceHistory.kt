package com.pricetracker.app.domain.model

data class PriceHistory(
    val id: Long = 0,
    val productId: Long,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
)
