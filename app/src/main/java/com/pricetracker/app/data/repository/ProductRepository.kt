package com.pricetracker.app.data.repository

import android.util.Log
import com.pricetracker.app.data.local.PriceHistoryDao
import com.pricetracker.app.data.local.ProductDao
import com.pricetracker.app.data.model.PriceHistory
import com.pricetracker.app.data.model.Product
import com.pricetracker.app.data.scraper.ScraperManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val scraperManager: ScraperManager
) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    fun getPriceHistory(productId: Long): Flow<List<PriceHistory>> =
        priceHistoryDao.getHistoryForProduct(productId)

    suspend fun addProductFromUrl(url: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val scraped = scraperManager.scrapeProduct(url)
                ?: return@withContext Result.failure(Exception("Could not fetch product details"))

            val product = Product(
                url = url,
                name = scraped.name,
                description = scraped.description,
                imageUrl = scraped.imageUrl,
                currentPrice = scraped.price,
                lowestPrice = scraped.price,
                highestPrice = scraped.price,
                store = scraped.store
            )
            val id = productDao.insertProduct(product)
            priceHistoryDao.insertPriceRecord(
                PriceHistory(productId = id, price = scraped.price)
            )
            Result.success(product.copy(id = id))
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error adding product", e)
            Result.failure(e)
        }
    }

    data class PriceUpdate(
        val product: Product,
        val oldPrice: Double,
        val newPrice: Double,
        val changed: Boolean
    )

    suspend fun refreshPrice(productId: Long): Result<PriceUpdate> = withContext(Dispatchers.IO) {
        try {
            val product = productDao.getProductById(productId)
                ?: return@withContext Result.failure(Exception("Product not found"))

            val scraped = scraperManager.scrapeProduct(product.url)
                ?: return@withContext Result.failure(Exception("Could not fetch price"))

            val oldPrice = product.currentPrice
            val newPrice = scraped.price
            val updated = product.copy(
                currentPrice = newPrice,
                name = scraped.name.ifBlank { product.name },
                imageUrl = scraped.imageUrl.ifBlank { product.imageUrl },
                description = scraped.description.ifBlank { product.description },
                lowestPrice = minOf(product.lowestPrice, newPrice),
                highestPrice = maxOf(product.highestPrice, newPrice),
                lastCheckedAt = System.currentTimeMillis()
            )
            productDao.updateProduct(updated)
            priceHistoryDao.insertPriceRecord(
                PriceHistory(productId = productId, price = newPrice)
            )
            Result.success(PriceUpdate(updated, oldPrice, newPrice, oldPrice != newPrice))
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error refreshing price", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: Long) {
        productDao.deleteProductById(productId)
    }

    suspend fun getAllProductsList(): List<Product> = productDao.getAllProductsList()
}
