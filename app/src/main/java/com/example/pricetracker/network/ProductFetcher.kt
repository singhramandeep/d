package com.example.pricetracker.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ProductFetcher(
    private val parser: ProductPageParser = ProductPageParser()
) {
    suspend fun fetch(url: String): ParsedProduct = withContext(Dispatchers.IO) {
        val source = UrlSourceDetector.detect(url)
            ?: throw IllegalArgumentException("Unsupported URL. Use amazon.in, flipkart.com, or myntra.com.")
        val html = download(url)
        return@withContext parser.parse(source = source, url = url, html = html)
    }

    private fun download(url: String): String {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
            )
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (error: Exception) {
            Log.e("ProductFetcher", "Failed to download product page: $url", error)
            throw IllegalStateException("Unable to fetch product page at the moment.", error)
        }
    }
}
