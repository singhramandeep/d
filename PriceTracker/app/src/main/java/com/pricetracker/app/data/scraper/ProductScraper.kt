package com.pricetracker.app.data.scraper

import android.util.Log
import com.pricetracker.app.domain.model.Platform
import com.pricetracker.app.domain.model.Product
import com.pricetracker.app.domain.model.toPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ScrapedProduct(
    val name: String,
    val imageUrl: String,
    val description: String,
    val currentPrice: Double,
    val originalPrice: Double,
    val currency: String = "₹"
)

@Singleton
class ProductScraper @Inject constructor(
    private val httpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "ProductScraper"
    }

    suspend fun scrapeProduct(url: String): ScrapedProduct? = withContext(Dispatchers.IO) {
        try {
            val platform = url.toPlatform()
            val doc = fetchDocument(url) ?: return@withContext null

            return@withContext when (platform) {
                Platform.AMAZON -> scrapeAmazon(doc)
                Platform.FLIPKART -> scrapeFlipkart(doc)
                Platform.MYNTRA -> scrapeMyntra(doc, url)
                Platform.UNKNOWN -> scrapeGeneric(doc)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping product: ${e.message}", e)
            null
        }
    }

    private fun fetchDocument(url: String): Document? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-IN,en;q=0.9,hi;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "HTTP ${response.code} for URL: $url")
                return null
            }
            val body = response.body?.string() ?: return null
            Jsoup.parse(body, url)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching document: ${e.message}", e)
            // Fallback: try Jsoup directly
            try {
                Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("Accept-Language", "en-IN,en;q=0.9")
                    .timeout(15000)
                    .get()
            } catch (e2: Exception) {
                Log.e(TAG, "Jsoup fallback also failed: ${e2.message}", e2)
                null
            }
        }
    }

    private fun scrapeAmazon(doc: Document): ScrapedProduct? {
        try {
            val name = doc.select("#productTitle").text().trim()
                .ifEmpty { doc.select("h1.a-size-large").text().trim() }
                .ifEmpty { doc.title().replace("Amazon.in : ", "").replace(" : Amazon.in", "") }

            val priceSelectors = listOf(
                ".a-price-whole",
                "#priceblock_dealprice",
                "#priceblock_ourprice",
                ".a-offscreen",
                "#corePriceDisplay_desktop_feature_div .a-price-whole",
                ".priceToPay .a-price-whole"
            )
            val priceText = priceSelectors.firstNotNullOfOrNull { sel ->
                doc.select(sel).firstOrNull()?.text()?.ifEmpty { null }
            } ?: "0"

            val originalPriceText = doc.select(".a-text-price .a-offscreen, #listPrice, .a-text-strike")
                .firstOrNull()?.text() ?: priceText

            val imageUrl = doc.select("#landingImage, #imgBlkFront, #main-image")
                .firstOrNull()?.attr("src")
                ?.ifEmpty { doc.select("#imgTagWrapperId img").attr("data-old-hires") }
                ?: doc.select("img[data-old-hires]").attr("data-old-hires")

            val description = doc.select("#feature-bullets .a-list-item").take(3)
                .joinToString(" • ") { it.text().trim() }
                .ifEmpty { doc.select("#productDescription p").firstOrNull()?.text() ?: name }

            val currentPrice = parsePrice(priceText)
            val originalPrice = parsePrice(originalPriceText).takeIf { it > 0 } ?: currentPrice

            if (name.isEmpty() && currentPrice <= 0) return null

            return ScrapedProduct(
                name = name.ifEmpty { "Amazon Product" },
                imageUrl = imageUrl,
                description = description,
                currentPrice = currentPrice,
                originalPrice = originalPrice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Amazon scrape error: ${e.message}", e)
            return null
        }
    }

    private fun scrapeFlipkart(doc: Document): ScrapedProduct? {
        try {
            val name = doc.select("span.B_NuCI, h1.yhB1nd").text().trim()
                .ifEmpty { doc.select("h1").firstOrNull()?.text()?.trim() ?: "" }
                .ifEmpty { doc.title().replace(" - Buy.*".toRegex(), "").trim() }

            val priceSelectors = listOf(
                "div._30jeq3._16Jk6d",
                "div._30jeq3",
                "._1vC4OE._3qQ9m1",
                "div._25b18c ._30jeq3",
                "._3I9_wc._27HOfX"
            )
            val priceText = priceSelectors.firstNotNullOfOrNull { sel ->
                doc.select(sel).firstOrNull()?.text()?.ifEmpty { null }
            } ?: "0"

            val originalPriceText = doc.select("div._3I9_wc, ._3auQ3N, ._1AtVbE ._3qQ9m1")
                .firstOrNull()?.text() ?: priceText

            val imageUrl = doc.select("img._396cs4, img._2r_T1I, div._3kidJX img, img.q6DClP")
                .firstOrNull()?.attr("src") ?: ""

            val description = doc.select("div._1mXcCf li, ._21Xx9V li, .RmoJUa li").take(3)
                .joinToString(" • ") { it.text().trim() }
                .ifEmpty { doc.select("div._1AN87F, div.X3BRps").firstOrNull()?.text() ?: name }

            val currentPrice = parsePrice(priceText)
            val originalPrice = parsePrice(originalPriceText).takeIf { it > 0 } ?: currentPrice

            if (name.isEmpty() && currentPrice <= 0) return null

            return ScrapedProduct(
                name = name.ifEmpty { "Flipkart Product" },
                imageUrl = imageUrl,
                description = description,
                currentPrice = currentPrice,
                originalPrice = originalPrice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Flipkart scrape error: ${e.message}", e)
            return null
        }
    }

    private fun scrapeMyntra(doc: Document, url: String): ScrapedProduct? {
        try {
            // Myntra is heavy JS - try to get metadata from page
            val name = doc.select("h1.pdp-name, .pdp-product-description-content h1")
                .text().trim()
                .ifEmpty {
                    doc.select("meta[property=og:title]").attr("content")
                        .ifEmpty { extractMyntraNameFromUrl(url) }
                }

            val priceText = doc.select("span.pdp-price strong, .pdp-mrp, span.pdp-discount-price")
                .firstOrNull()?.text()
                ?: doc.select("meta[property=product:price:amount]").attr("content")

            val originalPriceText = doc.select("span.pdp-mrp, del.pdp-price")
                .firstOrNull()?.text() ?: priceText

            val imageUrl = doc.select("meta[property=og:image]").attr("content")
                .ifEmpty { doc.select("img.pdp-img").firstOrNull()?.attr("src") ?: "" }

            val description = doc.select("meta[property=og:description]").attr("content")
                .ifEmpty { name }

            val currentPrice = parsePrice(priceText)
            val originalPrice = parsePrice(originalPriceText).takeIf { it > 0 } ?: currentPrice

            return ScrapedProduct(
                name = name.ifEmpty { "Myntra Product" },
                imageUrl = imageUrl,
                description = description,
                currentPrice = currentPrice,
                originalPrice = originalPrice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Myntra scrape error: ${e.message}", e)
            return null
        }
    }

    private fun scrapeGeneric(doc: Document): ScrapedProduct? {
        val name = doc.select("h1").firstOrNull()?.text()?.trim()
            ?: doc.select("meta[property=og:title]").attr("content")
            ?: doc.title()

        val priceText = doc.select("[itemprop=price], .price, #price, .product-price")
            .firstOrNull()?.text()
            ?: doc.select("meta[property=product:price:amount]").attr("content")
            ?: "0"

        val imageUrl = doc.select("meta[property=og:image]").attr("content")
            .ifEmpty { doc.select("[itemprop=image]").attr("src") }

        val description = doc.select("meta[property=og:description]").attr("content")
            .ifEmpty { doc.select("[itemprop=description]").text().take(200) }
            .ifEmpty { name }

        val currentPrice = parsePrice(priceText)

        return ScrapedProduct(
            name = name.ifEmpty { "Product" },
            imageUrl = imageUrl,
            description = description,
            currentPrice = currentPrice,
            originalPrice = currentPrice
        )
    }

    private fun extractMyntraNameFromUrl(url: String): String {
        return try {
            val path = url.substringAfter("myntra.com/").substringBefore("?")
            path.split("/").take(3).joinToString(" ") { 
                it.replace("-", " ").split(" ").joinToString(" ") { w -> 
                    w.replaceFirstChar { c -> c.uppercaseChar() } 
                }
            }
        } catch (e: Exception) {
            "Myntra Product"
        }
    }

    private fun parsePrice(priceText: String): Double {
        return try {
            priceText.replace(",", "")
                .replace("₹", "")
                .replace("Rs.", "")
                .replace("Rs", "")
                .replace("INR", "")
                .trim()
                .filter { it.isDigit() || it == '.' }
                .toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}
