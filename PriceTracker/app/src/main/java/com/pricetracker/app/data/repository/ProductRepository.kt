package com.pricetracker.app.data.repository

import android.util.Log
import com.pricetracker.app.data.db.PriceHistoryDao
import com.pricetracker.app.data.db.PriceHistoryEntity
import com.pricetracker.app.data.db.ProductDao
import com.pricetracker.app.data.db.ProductEntity
import com.pricetracker.app.data.scraper.ProductScraper
import com.pricetracker.app.domain.model.Platform
import com.pricetracker.app.domain.model.PriceHistory
import com.pricetracker.app.domain.model.Product
import com.pricetracker.app.domain.model.Result
import com.pricetracker.app.domain.model.toPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val scraper: ProductScraper
) {
    companion object {
        private const val TAG = "ProductRepository"
    }

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getProductById(id: Long): Flow<Product?> {
        return productDao.getProductById(id).map { it?.toDomain() }
    }

    fun getPriceHistory(productId: Long): Flow<List<PriceHistory>> {
        return priceHistoryDao.getPriceHistory(productId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addProduct(url: String): Result<Product> {
        return try {
            val scraped = scraper.scrapeProduct(url)
                ?: return Result.Error("Failed to fetch product details from this URL. The site may be blocking automated access.")

            if (scraped.currentPrice <= 0) {
                return Result.Error("Could not extract price from this product page. Try the direct product page URL.")
            }

            val platform = url.toPlatform()
            val entity = ProductEntity(
                url = url,
                name = scraped.name,
                imageUrl = scraped.imageUrl,
                description = scraped.description,
                currentPrice = scraped.currentPrice,
                originalPrice = scraped.originalPrice,
                currency = scraped.currency,
                platform = platform.name
            )
            val id = productDao.insertProduct(entity)

            priceHistoryDao.insertPriceHistory(
                PriceHistoryEntity(productId = id, price = scraped.currentPrice)
            )

            val product = entity.copy(id = id).toDomain()
            Log.d(TAG, "Product added: ${product.name} at ${product.currentPrice}")
            Result.Success(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product: ${e.message}", e)
            Result.Error("An error occurred: ${e.message}", e)
        }
    }

    suspend fun refreshProduct(productId: Long): Result<Product> {
        return try {
            val currentEntity = productDao.getProductById(productId).first()
                ?: return Result.Error("Product not found")

            val scraped = scraper.scrapeProduct(currentEntity.url)
                ?: return Result.Error("Failed to fetch updated price")

            val newPrice = scraped.currentPrice.takeIf { it > 0 } ?: currentEntity.currentPrice
            productDao.updatePrice(productId, newPrice, System.currentTimeMillis())

            if (scraped.currentPrice > 0 && scraped.currentPrice != currentEntity.currentPrice) {
                priceHistoryDao.insertPriceHistory(
                    PriceHistoryEntity(productId = productId, price = scraped.currentPrice)
                )
            }

            val updatedEntity = currentEntity.copy(
                currentPrice = newPrice,
                lastUpdated = System.currentTimeMillis()
            )
            Result.Success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing product: ${e.message}", e)
            Result.Error("Failed to refresh: ${e.message}", e)
        }
    }

    suspend fun refreshAllActiveProducts(): List<Pair<Product, Double>> {
        val activeProducts = productDao.getActiveProducts()
        val priceDrop = mutableListOf<Pair<Product, Double>>()

        for (entity in activeProducts) {
            try {
                val scraped = scraper.scrapeProduct(entity.url) ?: continue
                if (scraped.currentPrice > 0) {
                    val previousPrice = entity.currentPrice
                    val newPrice = scraped.currentPrice

                    productDao.updatePrice(entity.id, newPrice, System.currentTimeMillis())
                    priceHistoryDao.insertPriceHistory(
                        PriceHistoryEntity(productId = entity.id, price = newPrice)
                    )

                    if (newPrice < previousPrice) {
                        priceDrop.add(Pair(entity.toDomain(), previousPrice))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing ${entity.name}: ${e.message}", e)
            }
        }
        return priceDrop
    }

    suspend fun deleteProduct(productId: Long) {
        productDao.deleteProduct(productId)
    }

    suspend fun getLowestPrice(productId: Long): Double? {
        return priceHistoryDao.getLowestPrice(productId)
    }

    suspend fun getHighestPrice(productId: Long): Double? {
        return priceHistoryDao.getHighestPrice(productId)
    }

    private fun ProductEntity.toDomain() = Product(
        id = id,
        url = url,
        name = name,
        imageUrl = imageUrl,
        description = description,
        currentPrice = currentPrice,
        originalPrice = originalPrice,
        currency = currency,
        platform = Platform.valueOf(platform),
        lastUpdated = lastUpdated,
        isTracking = isTracking
    )

    private fun PriceHistoryEntity.toDomain() = PriceHistory(
        id = id,
        productId = productId,
        price = price,
        timestamp = timestamp
    )
}
