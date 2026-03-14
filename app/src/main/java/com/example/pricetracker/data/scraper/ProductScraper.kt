package com.example.pricetracker.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URI
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
        val jsonLdMetadata = extractJsonLdMetadata(document)

        val title = extractTitle(document) ?: jsonLdMetadata.title
        val price = when {
            "amazon.in" in host -> extractAmazonPrice(document)
            "flipkart.com" in host -> extractFlipkartPrice(document)
            else -> extractGenericPrice(document)
        } ?: jsonLdMetadata.pricePaise
        val imageUrl = extractImageUrl(document) ?: jsonLdMetadata.imageUrl

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
        val amazonDynamicImage = runCatching {
            val raw = document.selectFirst("#landingImage")?.attr("data-a-dynamic-image")
            if (raw.isNullOrBlank()) null else JSONObject(raw).keys().asSequence().toList().firstOrNull()
        }.getOrNull()

        val rawCandidates = listOfNotNull(
            amazonDynamicImage,
            document.selectFirst("#landingImage")?.attr("data-old-hires"),
            document.selectFirst("#landingImage")?.absUrl("src"),
            document.selectFirst("#imgBlkFront")?.absUrl("src"),
            document.selectFirst("img._396cs4")?.absUrl("src"),
            document.selectFirst("img._53J4C-")?.absUrl("src"),
            document.selectFirst("meta[property=og:image]")?.attr("content"),
            document.selectFirst("meta[name=twitter:image]")?.attr("content"),
            document.selectFirst("link[rel=image_src]")?.attr("href")
        )

        rawCandidates.forEach { raw ->
            val image = normalizeImageUrl(raw, document.location())
            if (!image.isNullOrBlank()) return image
        }
        return null
    }

    private fun normalizeImageUrl(raw: String?, baseUrl: String): String? {
        if (raw.isNullOrBlank()) return null
        val value = raw.trim()
        val normalized = if (value.contains(",")) {
            // Handles srcset-like values by taking the first candidate URL.
            value.substringBefore(",").substringBefore(" ").trim()
        } else {
            value
        }

        return when {
            normalized.startsWith("//") -> "https:$normalized"
            normalized.startsWith("/") -> {
                val uri = runCatching { URI(baseUrl) }.getOrNull()
                val host = uri?.host ?: return normalized
                val scheme = uri.scheme ?: "https"
                "$scheme://$host$normalized"
            }
            normalized.startsWith("http://") || normalized.startsWith("https://") -> normalized
            else -> runCatching { URI(baseUrl).resolve(normalized).toString() }.getOrDefault(normalized)
        }
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

    private fun extractJsonLdMetadata(document: org.jsoup.nodes.Document): ScrapedProduct {
        val scripts = document.select("script[type=application/ld+json]")
        scripts.forEach { script ->
            val jsonText = script.data().ifBlank { script.html() }.trim()
            if (jsonText.isBlank()) return@forEach
            val parsed = runCatching { JSONTokener(jsonText).nextValue() }.getOrNull() ?: return@forEach
            val metadata = parseJsonLdNode(parsed, document.location())
            if (metadata != null) return metadata
        }
        return ScrapedProduct(title = null, pricePaise = null, imageUrl = null)
    }

    private fun parseJsonLdNode(node: Any?, baseUrl: String): ScrapedProduct? {
        return when (node) {
            is JSONObject -> parseJsonLdObject(node, baseUrl)
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    val found = parseJsonLdNode(node.opt(i), baseUrl)
                    if (found != null) return found
                }
                null
            }

            else -> null
        }
    }

    private fun parseJsonLdObject(obj: JSONObject, baseUrl: String): ScrapedProduct? {
        val typeRaw = obj.opt("@type")
        val isProduct = when (typeRaw) {
            is String -> typeRaw.contains("Product", ignoreCase = true)
            is JSONArray -> (0 until typeRaw.length()).any {
                typeRaw.optString(it).contains("Product", ignoreCase = true)
            }

            else -> false
        }

        if (isProduct) {
            val title = obj.optString("name").ifBlank { null }
            val imageUrl = extractJsonLdImageUrl(obj.opt("image"), baseUrl)
            val price = extractJsonLdPrice(obj.opt("offers"))
            return ScrapedProduct(
                title = title,
                pricePaise = price,
                imageUrl = imageUrl
            )
        }

        val graphNode = obj.opt("@graph")
        if (graphNode != null) {
            val graphProduct = parseJsonLdNode(graphNode, baseUrl)
            if (graphProduct != null) return graphProduct
        }
        return null
    }

    private fun extractJsonLdImageUrl(imageNode: Any?, baseUrl: String): String? {
        return when (imageNode) {
            is String -> normalizeImageUrl(imageNode, baseUrl)
            is JSONArray -> {
                for (i in 0 until imageNode.length()) {
                    val fromArray = extractJsonLdImageUrl(imageNode.opt(i), baseUrl)
                    if (!fromArray.isNullOrBlank()) return fromArray
                }
                null
            }

            is JSONObject -> normalizeImageUrl(
                imageNode.optString("url").ifBlank { imageNode.optString("@id") },
                baseUrl
            )

            else -> null
        }
    }

    private fun extractJsonLdPrice(offersNode: Any?): Long? {
        return when (offersNode) {
            is JSONObject -> {
                parsePriceToPaise(offersNode.optString("price").ifBlank { null })
            }

            is JSONArray -> {
                for (i in 0 until offersNode.length()) {
                    val price = extractJsonLdPrice(offersNode.opt(i))
                    if (price != null) return price
                }
                null
            }

            else -> null
        }
    }
}
