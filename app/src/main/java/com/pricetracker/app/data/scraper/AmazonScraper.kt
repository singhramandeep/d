package com.pricetracker.app.data.scraper

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class AmazonScraper : PriceScraper {

    override fun canHandle(url: String): Boolean {
        return url.contains("amazon.in") || url.contains("amazon.com")
    }

    override suspend fun scrape(url: String): ScrapedProduct? {
        return try {
            val doc = fetchDocument(url)
            val name = extractName(doc)
            val price = extractPrice(doc)
            val imageUrl = extractImage(doc)
            val description = extractDescription(doc)

            if (name.isNotBlank() && price > 0) {
                ScrapedProduct(name, price, imageUrl, description, "Amazon")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AmazonScraper", "Failed to scrape: ${e.message}", e)
            null
        }
    }

    private fun fetchDocument(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36")
            .header("Accept-Language", "en-IN,en;q=0.9")
            .header("Accept", "text/html,application/xhtml+xml")
            .timeout(15000)
            .get()
    }

    private fun extractName(doc: Document): String {
        return doc.selectFirst("#productTitle")?.text()?.trim()
            ?: doc.selectFirst("span#productTitle")?.text()?.trim()
            ?: doc.selectFirst("h1#title span")?.text()?.trim()
            ?: ""
    }

    private fun extractPrice(doc: Document): Double {
        val selectors = listOf(
            "span.a-price-whole",
            "#priceblock_ourprice",
            "#priceblock_dealprice",
            "span.a-offscreen",
            "#corePrice_feature_div span.a-offscreen",
            "#corePriceDisplay_desktop_feature_div span.a-offscreen",
            "span.priceToPay span.a-offscreen"
        )
        for (selector in selectors) {
            val text = doc.selectFirst(selector)?.text() ?: continue
            val cleaned = text.replace("[^0-9.,]".toRegex(), "")
                .replace(",", "")
                .trim()
            val price = cleaned.toDoubleOrNull()
            if (price != null && price > 0) return price
        }
        return 0.0
    }

    private fun extractImage(doc: Document): String {
        return doc.selectFirst("#landingImage")?.attr("src")
            ?: doc.selectFirst("#imgBlkFront")?.attr("src")
            ?: doc.selectFirst("#main-image")?.attr("src")
            ?: doc.selectFirst("img#landingImage")?.attr("data-old-hires")
            ?: ""
    }

    private fun extractDescription(doc: Document): String {
        val bullets = doc.select("#feature-bullets ul li span.a-list-item")
        if (bullets.isNotEmpty()) {
            return bullets.take(5).joinToString("\n") { "• ${it.text().trim()}" }
        }
        return doc.selectFirst("#productDescription p")?.text()?.trim()
            ?: doc.selectFirst("meta[name=description]")?.attr("content")?.trim()
            ?: ""
    }
}
