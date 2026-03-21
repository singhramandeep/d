package com.pricetracker.app.data

import android.util.Log
import com.pricetracker.app.scraper.ProductScraper
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
) {
    private val tag = "ProductRepository"

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getAllProductsList(): List<Product> = productDao.getAllProductsList()

    suspend fun getProductById(id: Long) = productDao.getProductById(id)

    fun getPriceHistory(productId: Long): Flow<List<PriceHistory>> =
        productDao.getPriceHistory(productId)

    suspend fun addProductByUrl(url: String): Result<Long> {
        val cleanUrl = url.trim().substringBefore("?")
        if (!ProductScraper.isValidProductUrl(cleanUrl)) {
            return Result.failure(IllegalArgumentException("Invalid product URL"))
        }
        productDao.getProductByUrl(cleanUrl)?.let { existing ->
            return Result.success(existing.id)
        }
        return ProductScraper.scrapeProduct(cleanUrl).fold(
            onSuccess = { scraped ->
                val product = Product(
                    url = cleanUrl,
                    title = scraped.title,
                    description = scraped.description,
                    imageUrl = scraped.imageUrl,
                    currentPrice = scraped.price,
                    originalPrice = scraped.originalPrice,
                    source = scraped.source
                )
                val id = productDao.insertProduct(product)
                productDao.insertPriceHistory(PriceHistory(productId = id, price = scraped.price))
                Result.success(id)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun refreshProductPrice(productId: Long): Boolean {
        val product = productDao.getProductById(productId) ?: return false
        return ProductScraper.scrapeProduct(product.url).fold(
            onSuccess = { scraped ->
                val oldPrice = product.currentPrice
                val newPrice = scraped.price
                productDao.updateProduct(
                    product.copy(
                        currentPrice = newPrice,
                        lastCheckedAt = System.currentTimeMillis()
                    )
                )
                productDao.insertPriceHistory(PriceHistory(productId = productId, price = newPrice))
                oldPrice != newPrice
            },
            onFailure = {
                Log.w(tag, "Refresh failed for ${product.url}", it)
                false
            }
        )
    }

    suspend fun refreshAllProducts(): List<Long> {
        val products = productDao.getAllProductsList()
        val changedIds = mutableListOf<Long>()
        for (product in products) {
            if (refreshProductPrice(product.id)) {
                changedIds.add(product.id)
            }
        }
        return changedIds
    }

    suspend fun deleteProduct(productId: Long) {
        productDao.deleteProduct(productId)
    }
}
