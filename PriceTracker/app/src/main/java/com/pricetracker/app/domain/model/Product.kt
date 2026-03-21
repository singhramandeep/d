package com.pricetracker.app.domain.model

data class Product(
    val id: Long = 0,
    val url: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val currentPrice: Double,
    val originalPrice: Double,
    val currency: String = "₹",
    val platform: Platform,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isTracking: Boolean = true
)

enum class Platform(val displayName: String) {
    AMAZON("Amazon"),
    FLIPKART("Flipkart"),
    MYNTRA("Myntra"),
    UNKNOWN("Unknown")
}

fun String.toPlatform(): Platform {
    return when {
        contains("amazon", ignoreCase = true) -> Platform.AMAZON
        contains("flipkart", ignoreCase = true) -> Platform.FLIPKART
        contains("myntra", ignoreCase = true) -> Platform.MYNTRA
        else -> Platform.UNKNOWN
    }
}
