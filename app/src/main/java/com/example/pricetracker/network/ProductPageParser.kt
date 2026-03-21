package com.example.pricetracker.network

class ProductPageParser {
    fun parse(source: ProductSource, url: String, html: String): ParsedProduct {
        val jsonLd = extractJsonLdBlock(html)
        val title = extractTitle(html, jsonLd)
        val imageUrl = extractImageUrl(html, jsonLd)
        val price = extractPrice(source, html, jsonLd)
        return ParsedProduct(
            source = source,
            url = url,
            title = title ?: throw IllegalArgumentException("Unable to parse title for $url"),
            imageUrl = imageUrl ?: throw IllegalArgumentException("Unable to parse image URL for $url"),
            price = price ?: throw IllegalArgumentException("Unable to parse price for $url")
        )
    }

    private fun extractTitle(html: String, jsonLd: String?): String? {
        val fromJson = extractJsonField(jsonLd, "name")
        if (!fromJson.isNullOrBlank()) return cleanTitle(fromJson)
        val fromOg = extractMetaContent(html, "og:title")
        if (!fromOg.isNullOrBlank()) return cleanTitle(fromOg)
        val title = Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
        return title?.trim()?.let(::cleanTitle)?.takeIf { it.isNotBlank() }
    }

    private fun extractImageUrl(html: String, jsonLd: String?): String? {
        val fromJson = extractJsonField(jsonLd, "image")
        if (!fromJson.isNullOrBlank()) return fromJson
        val fromOg = extractMetaContent(html, "og:image")
        if (!fromOg.isNullOrBlank()) return fromOg
        return null
    }

    private fun extractPrice(source: ProductSource, html: String, jsonLd: String?): Double? {
        val fromJson = extractJsonField(jsonLd, "price")?.let(::toAmount)
        if (fromJson != null) return fromJson
        val sourcePatterns = when (source) {
            ProductSource.AMAZON -> listOf(
                """a-price-whole[^>]*>\s*([0-9,]+)""",
                """₹\s?([0-9,]+(?:\.[0-9]{1,2})?)"""
            )
            ProductSource.FLIPKART, ProductSource.MYNTRA -> listOf(
                """₹\s?([0-9,]+(?:\.[0-9]{1,2})?)""",
                """"price"\s*:\s*"([0-9,]+(?:\.[0-9]{1,2})?)""""
            )
        }
        return sourcePatterns.firstNotNullOfOrNull { pattern ->
            Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .find(html)
                ?.groupValues
                ?.getOrNull(1)
                ?.let(::toAmount)
        }
    }

    private fun extractJsonLdBlock(html: String): String? {
        val regex = Regex(
            "<script[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(.*?)</script>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        return regex.find(html)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun extractJsonField(json: String?, field: String): String? {
        if (json.isNullOrBlank()) return null
        val direct = Regex(""""$field"\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
            .find(json)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
        if (!direct.isNullOrBlank()) return direct
        return Regex(""""$field"\s*:\s*\[\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
            .find(json)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
    }

    private fun extractMetaContent(html: String, property: String): String? {
        val regex = Regex(
            """<meta[^>]+property=["']$property["'][^>]+content=["']([^"']+)["'][^>]*>""",
            RegexOption.IGNORE_CASE
        )
        return regex.find(html)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun toAmount(raw: String): Double? {
        val normalized = raw.replace(",", "").replace("₹", "").trim()
        return normalized.toDoubleOrNull()
    }

    private fun cleanTitle(value: String): String {
        return value.replace(Regex("""\s*[-|]\s*(Amazon\.in|Flipkart|Myntra).*""", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
