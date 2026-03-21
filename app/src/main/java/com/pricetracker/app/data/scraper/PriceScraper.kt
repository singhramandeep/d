package com.pricetracker.app.data.scraper

data class ScrapedProduct(
    val name: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val store: String
)

interface PriceScraper {
    fun canHandle(url: String): Boolean
    suspend fun scrape(url: String): ScrapedProduct?
}
