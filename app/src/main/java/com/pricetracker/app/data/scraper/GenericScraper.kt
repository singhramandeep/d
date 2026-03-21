package com.pricetracker.app.data.scraper

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GenericScraper : PriceScraper {

    override fun canHandle(url: String): Boolean = true

    override suspend fun scrape(url: String): ScrapedProduct? {
        return try {
            val doc = fetchDocument(url)
            val name = extractName(doc)
            val price = extractPrice(doc)
            val imageUrl = extractImage(doc)
            val description = extractDescription(doc)
            val store = java.net.URI(url).host?.replace("www.", "")?.split(".")?.firstOrNull()
                ?.replaceFirstChar { it.uppercase() } ?: "Web"

            if (name.isNotBlank() && price > 0) {
                ScrapedProduct(name, price, imageUrl, description, store)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("GenericScraper", "Failed to scrape: ${e.message}", e)
            null
        }
    }

    private fun fetchDocument(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36")
            .timeout(15000)
            .get()
    }

    private fun extractName(doc: Document): String {
        return doc.selectFirst("meta[property=og:title]")?.attr("content")?.trim()
            ?: doc.selectFirst("h1")?.text()?.trim()
            ?: doc.title().trim()
    }

    private fun extractPrice(doc: Document): Double {
        val metaPrice = doc.selectFirst("meta[property=product:price:amount]")?.attr("content")
        if (metaPrice != null) {
            metaPrice.toDoubleOrNull()?.let { if (it > 0) return it }
        }
        val pricePattern = "[₹$]\\s*[\\d,]+\\.?\\d*".toRegex()
        val bodyText = doc.body()?.text() ?: return 0.0
        val match = pricePattern.find(bodyText)?.value ?: return 0.0
        return match.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }

    private fun extractImage(doc: Document): String {
        return doc.selectFirst("meta[property=og:image]")?.attr("content") ?: ""
    }

    private fun extractDescription(doc: Document): String {
        return doc.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
            ?: doc.selectFirst("meta[name=description]")?.attr("content")?.trim()
            ?: ""
    }
}
