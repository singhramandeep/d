package com.example.pricetracker.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.Locale

data class ScrapedProduct(
    val title: String?,
    val pricePaise: Long?,
    val imageUrl: String?
)

class ProductScraper(
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun scrape(url: String): ScrapedProduct? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14; PricePulse) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
            )
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return@withContext null
        }

        val body = response.body?.string() ?: return@withContext null
        val document = Jsoup.parse(body, url)
        val host = document.location().lowercase(Locale.US)

        val title = extractTitle(document)
        val price = when {
            "amazon.in" in host -> extractAmazonPrice(document)
            "flipkart.com" in host -> extractFlipkartPrice(document)
            else -> extractGenericPrice(document)
        }
        val imageUrl = extractImageUrl(document)

        ScrapedProduct(title = title, pricePaise = price, imageUrl = imageUrl)
    }

    private fun extractTitle(document: org.jsoup.nodes.Document): String? {
        val candidates = listOf(
            document.selectFirst("#productTitle")?.text(),
            document.selectFirst("span.B_NuCI")?.text(),
            document.selectFirst("meta[property=og:title]")?.attr("content"),
            document.title()
        )
        return candidates.firstOrNull { !it.isNullOrBlank() }?.trim()
    }

    private fun extractAmazonPrice(document: org.jsoup.nodes.Document): Long? {
        val candidates = listOf(
            document.selectFirst("#corePriceDisplay_desktop_feature_div .a-offscreen")?.text(),
            document.selectFirst("#priceblock_dealprice")?.text(),
            document.selectFirst("#priceblock_ourprice")?.text(),
            document.selectFirst(".a-price .a-offscreen")?.text(),
            document.selectFirst("meta[property=product:price:amount]")?.attr("content")
        )
        return candidates.firstNotNullOfOrNull { parsePriceToPaise(it) } ?: extractGenericPrice(document)
    }

    private fun extractFlipkartPrice(document: org.jsoup.nodes.Document): Long? {
        val candidates = listOf(
            document.selectFirst("div.Nx9bqj")?.text(),
            document.selectFirst("div._30jeq3")?.text(),
            document.selectFirst("meta[property=product:price:amount]")?.attr("content"),
            document.selectFirst("meta[itemprop=price]")?.attr("content")
        )
        return candidates.firstNotNullOfOrNull { parsePriceToPaise(it) } ?: extractGenericPrice(document)
    }

    private fun extractGenericPrice(document: org.jsoup.nodes.Document): Long? {
        val metaCandidates = listOf(
            document.selectFirst("meta[property=product:price:amount]")?.attr("content"),
            document.selectFirst("meta[itemprop=price]")?.attr("content")
        )
        metaCandidates.firstNotNullOfOrNull { parsePriceToPaise(it) }?.let { return it }

        val rupeeRegex = Regex("(?:₹|Rs\\.?|INR)\\s*([0-9][0-9,]*(?:\\.\\d{1,2})?)")
        val hit = rupeeRegex.find(document.text())?.groupValues?.getOrNull(1)
        return parsePriceToPaise(hit)
    }

    private fun extractImageUrl(document: org.jsoup.nodes.Document): String? {
        val rawCandidates = listOf(
            document.selectFirst("#landingImage")?.attr("src"),
            document.selectFirst("#imgBlkFront")?.attr("src"),
            document.selectFirst("img._396cs4")?.attr("src"),
            document.selectFirst("meta[property=og:image]")?.attr("content"),
            document.selectFirst("meta[name=twitter:image]")?.attr("content")
        )

        rawCandidates.forEach { raw ->
            val image = normalizeImageUrl(raw)
            if (!image.isNullOrBlank()) return image
        }
        return null
    }

    private fun normalizeImageUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val value = raw.trim()
        if (value.contains(",")) {
            // Handles srcset-like values by taking the first candidate URL.
            return value.substringBefore(",").substringBefore(" ").trim()
        }
        return value
    }

    private fun parsePriceToPaise(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw
            .replace(",", "")
            .replace("₹", "")
            .replace("INR", "", ignoreCase = true)
            .replace("Rs.", "", ignoreCase = true)
            .replace("Rs", "", ignoreCase = true)
            .trim()
        val value = cleaned.toBigDecimalOrNull() ?: return null
        return value.multiply("100".toBigDecimal()).toLong()
    }
}
