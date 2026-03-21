package com.pricetracker.app.data.scraper

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MyntraScraper : PriceScraper {

    override fun canHandle(url: String): Boolean {
        return url.contains("myntra.com")
    }

    override suspend fun scrape(url: String): ScrapedProduct? {
        return try {
            val doc = fetchDocument(url)
            val name = extractName(doc)
            val price = extractPrice(doc)
            val imageUrl = extractImage(doc)
            val description = extractDescription(doc)

            if (name.isNotBlank() && price > 0) {
                ScrapedProduct(name, price, imageUrl, description, "Myntra")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("MyntraScraper", "Failed to scrape: ${e.message}", e)
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
        return doc.selectFirst("h1.pdp-title")?.text()?.trim()
            ?: doc.selectFirst("h1.pdp-name")?.text()?.trim()
            ?: doc.selectFirst("title")?.text()?.split("-")?.firstOrNull()?.trim()
            ?: ""
    }

    private fun extractPrice(doc: Document): Double {
        val selectors = listOf(
            "span.pdp-price strong",
            "span.pdp-discount-container span",
            "span.pdp-mrp strong",
            "span.pdp-price",
            "meta[property=product:price:amount]"
        )
        for (selector in selectors) {
            val el = doc.selectFirst(selector) ?: continue
            val text = if (selector.startsWith("meta")) el.attr("content") else el.text()
            val cleaned = text.replace("[^0-9.,]".toRegex(), "")
                .replace(",", "")
                .trim()
            val price = cleaned.toDoubleOrNull()
            if (price != null && price > 0) return price
        }
        return 0.0
    }

    private fun extractImage(doc: Document): String {
        return doc.selectFirst("img.image-grid-image")?.attr("src")
            ?: doc.selectFirst("div.image-grid-imageContainer img")?.attr("src")
            ?: doc.selectFirst("meta[property=og:image]")?.attr("content")
            ?: ""
    }

    private fun extractDescription(doc: Document): String {
        return doc.selectFirst("p.pdp-product-description-content")?.text()?.trim()
            ?: doc.selectFirst("meta[name=description]")?.attr("content")?.trim()
            ?: ""
    }
}
