package com.example.pricetracker.network

import java.net.URI

object UrlSourceDetector {
    fun detect(url: String): ProductSource? {
        val host = runCatching { URI(url).host.orEmpty().lowercase() }.getOrNull() ?: return null
        return when {
            host.endsWith("amazon.in") -> ProductSource.AMAZON
            host.endsWith("flipkart.com") -> ProductSource.FLIPKART
            host.endsWith("myntra.com") -> ProductSource.MYNTRA
            else -> null
        }
    }
}
