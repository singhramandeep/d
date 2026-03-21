package com.pricetracker.app.data.scraper

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScraperManager @Inject constructor() {

    private val scrapers: List<PriceScraper> = listOf(
        AmazonScraper(),
        FlipkartScraper(),
        MyntraScraper(),
        GenericScraper()
    )

    suspend fun scrapeProduct(url: String): ScrapedProduct? {
        val scraper = scrapers.firstOrNull { it.canHandle(url) } ?: return null
        return scraper.scrape(url)
    }
}
