package com.pricetracker.app.data.scraper

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class FlipkartScraper : PriceScraper {

    override fun canHandle(url: String): Boolean {
        return url.contains("flipkart.com")
    }

    override suspend fun scrape(url: String): ScrapedProduct? {
        return try {
            val doc = fetchDocument(url)
            val name = extractName(doc)
            val price = extractPrice(doc)
            val imageUrl = extractImage(doc)
            val description = extractDescription(doc)

            if (name.isNotBlank() && price > 0) {
                ScrapedProduct(name, price, imageUrl, description, "Flipkart")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FlipkartScraper", "Failed to scrape: ${e.message}", e)
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
        return doc.selectFirst("span.VU-ZEz")?.text()?.trim()
            ?: doc.selectFirst("span.B_NuCI")?.text()?.trim()
            ?: doc.selectFirst("h1.yhB1nd span")?.text()?.trim()
            ?: doc.selectFirst("h1 span")?.text()?.trim()
            ?: doc.title().split("-").firstOrNull()?.trim()
            ?: ""
    }

    private fun extractPrice(doc: Document): Double {
        val selectors = listOf(
            "div.Nx9bqj.CxhGGd",
            "div._30jeq3._16Jk6d",
            "div._30jeq3",
            "div.Nx9bqj",
            "span._30jeq3"
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
        return doc.selectFirst("img.DByuf4.IZexXJ.jLEJ7H")?.attr("src")
            ?: doc.selectFirst("img._396cs4._2amPTt._3qGmMb")?.attr("src")
            ?: doc.selectFirst("img._2r_T1I")?.attr("src")
            ?: doc.selectFirst("div._3kidJX img")?.attr("src")
            ?: doc.selectFirst("img.q6DClP")?.attr("src")
            ?: ""
    }

    private fun extractDescription(doc: Document): String {
        val highlights = doc.select("li._7eSDEz")
        if (highlights.isNotEmpty()) {
            return highlights.take(5).joinToString("\n") { "• ${it.text().trim()}" }
        }
        val altHighlights = doc.select("div._2418kt li")
        if (altHighlights.isNotEmpty()) {
            return altHighlights.take(5).joinToString("\n") { "• ${it.text().trim()}" }
        }
        return doc.selectFirst("meta[name=description]")?.attr("content")?.trim() ?: ""
    }
}
