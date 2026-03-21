package com.pricetracker.app.scraper

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.Connection
import java.net.URL

data class ScrapedProduct(
    val title: String,
    val description: String,
    val imageUrl: String,
    val price: Double,
    val originalPrice: Double?,
    val source: String
)

object ProductScraper {
    private const val TAG = "ProductScraper"
    private val USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    fun getSourceFromUrl(url: String): String? {
        return when {
            url.contains("flipkart.com") -> "flipkart"
            url.contains("amazon.in") -> "amazon"
            url.contains("myntra.com") -> "myntra"
            else -> null
        }
    }

    fun isValidProductUrl(url: String): Boolean {
        val cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http")) return false
        return getSourceFromUrl(cleanUrl) != null
    }

    suspend fun scrapeProduct(url: String): Result<ScrapedProduct> = withContext(Dispatchers.IO) {
        try {
            val source = getSourceFromUrl(url) ?: return@withContext Result.failure(
                IllegalArgumentException("Unsupported URL: $url")
            )
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("https://www.google.com/")
                .timeout(15000)
                .followRedirects(true)
                .get()

            val result = when (source) {
                "flipkart" -> scrapeFlipkart(doc)
                "amazon" -> scrapeAmazon(doc)
                "myntra" -> scrapeMyntra(doc)
                else -> null
            }

            result?.let { Result.success(it) }
                ?: Result.failure(Exception("Could not extract product data"))
        } catch (e: Exception) {
            Log.e(TAG, "Scrape failed for $url", e)
            Result.failure(e)
        }
    }

    private fun scrapeFlipkart(doc: org.jsoup.nodes.Document): ScrapedProduct? {
        val title = doc.select("meta[property=og:title]").attr("content")
            .ifEmpty { doc.select("span.B_NuCI").first()?.text() ?: "" }
        val imageUrl = doc.select("meta[property=og:image]").attr("content")
            .ifEmpty { doc.select("img._396cs4").attr("src") }
        val description = doc.select("meta[property=og:description]").attr("content")
            .ifEmpty { doc.select("div._1mXcCf").first()?.text() ?: "" }

        var price = 0.0
        doc.select("meta[itemprop=price]").first()?.attr("content")?.let {
            price = parsePrice(it)
        }
        if (price == 0.0) {
            doc.select("div._30jeq3._16Jk6d").first()?.text()?.let {
                price = parsePrice(it)
            }
        }
        if (price == 0.0) {
            doc.select("[class*=_30jeq3]").first()?.text()?.let {
                price = parsePrice(it)
            }
        }

        var originalPrice: Double? = null
        doc.select("div._3I9_wc._2p6lqe").first()?.text()?.let {
            originalPrice = parsePrice(it).takeIf { p -> p > 0 }
        }

        return if (title.isNotEmpty() && price > 0) {
            ScrapedProduct(title, description, imageUrl, price, originalPrice, "flipkart")
        } else null
    }

    private fun scrapeAmazon(doc: org.jsoup.nodes.Document): ScrapedProduct? {
        val title = doc.select("meta[property=og:title]").attr("content")
            .ifEmpty { doc.select("#productTitle").first()?.text()?.trim() ?: "" }
        val imageUrl = doc.select("meta[property=og:image]").attr("content")
            .ifEmpty { doc.select("#landingImage").attr("src") }
        val description = doc.select("meta[property=og:description]").attr("content")
            .ifEmpty { doc.select("#productDescription p").first()?.text()?.trim() ?: "" }

        var price = 0.0
        doc.select("#priceblock_ourprice, #priceblock_dealprice, #priceblock_saleprice").first()?.text()?.let {
            price = parsePrice(it)
        }
        if (price == 0.0) {
            doc.select(".a-price .a-offscreen").first()?.text()?.let {
                price = parsePrice(it)
            }
        }
        if (price == 0.0) {
            doc.select("span.a-price-whole").first()?.text()?.let {
                price = parsePrice(it)
            }
        }
        if (price == 0.0) {
            doc.select("meta[property=product:price:amount]").attr("content").let {
                if (it.isNotEmpty()) price = it.toDoubleOrNull() ?: 0.0
            }
        }

        var originalPrice: Double? = null
        doc.select("span.a-text-price[data-a-strike=true]").first()?.text()?.let {
            originalPrice = parsePrice(it).takeIf { p -> p > 0 }
        }

        return if (title.isNotEmpty() && price > 0) {
            ScrapedProduct(title, description, imageUrl, price, originalPrice, "amazon")
        } else null
    }

    private fun scrapeMyntra(doc: org.jsoup.nodes.Document): ScrapedProduct? {
        val title = doc.select("meta[property=og:title]").attr("content")
            .ifEmpty { doc.select("h1.pdp-title").first()?.text()?.trim() ?: "" }
        val imageUrl = doc.select("meta[property=og:image]").attr("content")
            .ifEmpty { doc.select("img.image-image").first()?.attr("src") ?: "" }
        val description = doc.select("meta[property=og:description]").attr("content")
            .ifEmpty { doc.select("p.pdp-product-description-content").first()?.text() ?: "" }

        var price = 0.0
        doc.select("span.pdp-price").first()?.text()?.let {
            price = parsePrice(it)
        }
        if (price == 0.0) {
            doc.select("[class*=pdp-price]").first()?.text()?.let {
                price = parsePrice(it)
            }
        }
        if (price == 0.0) {
            doc.select("meta[property=product:price:amount]").attr("content").let {
                if (it.isNotEmpty()) price = it.toDoubleOrNull() ?: 0.0
            }
        }

        var originalPrice: Double? = null
        doc.select("span.pdp-mrp").first()?.text()?.let {
            originalPrice = parsePrice(it).takeIf { p -> p > 0 }
        }

        return if (title.isNotEmpty() && price > 0) {
            ScrapedProduct(title, description, imageUrl, price, originalPrice, "myntra")
        } else null
    }

    private fun parsePrice(text: String): Double {
        val cleaned = text.replace(Regex("[^\\d.]"), "")
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}
