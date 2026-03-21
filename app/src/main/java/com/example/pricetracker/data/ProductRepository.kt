package com.example.pricetracker.data

import android.util.Log
import com.example.pricetracker.domain.HistoryRange
import com.example.pricetracker.network.ProductFetcher
import com.example.pricetracker.notifications.PriceNotificationHelper
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val historyDao: PriceHistoryDao,
    private val fetcher: ProductFetcher,
    private val notifications: PriceNotificationHelper
) {
    fun observeProducts(): Flow<List<ProductEntity>> = productDao.observeProducts()

    fun observeProduct(productId: Long): Flow<ProductEntity?> = productDao.observeProduct(productId)

    fun observeHistory(productId: Long, range: HistoryRange): Flow<List<PriceHistoryEntity>> {
        val from = range.toStartTime(System.currentTimeMillis())
        return if (from == null) historyDao.observeHistory(productId)
        else historyDao.observeHistoryFrom(productId, from)
    }

    suspend fun addProduct(url: String): Long {
        val normalized = url.trim()
        require(normalized.startsWith("http")) { "Please enter a valid product URL." }
        val existing = productDao.getByUrl(normalized)
        if (existing != null) {
            refreshProduct(existing.id, sendNotifications = false)
            return existing.id
        }
        val parsed = fetcher.fetch(normalized)
        val now = System.currentTimeMillis()
        val productId = productDao.insert(
            ProductEntity(
                url = parsed.url,
                source = parsed.source.name,
                title = parsed.title,
                imageUrl = parsed.imageUrl,
                currentPrice = parsed.price,
                createdAt = now,
                updatedAt = now,
                lastCheckedAt = now
            )
        )
        historyDao.insert(PriceHistoryEntity(productId = productId, price = parsed.price, timestamp = now))
        return productId
    }

    suspend fun refreshProduct(productId: Long, sendNotifications: Boolean = true): Boolean {
        val existing = productDao.getProduct(productId) ?: return false
        val parsed = fetcher.fetch(existing.url)
        val now = System.currentTimeMillis()
        val oldPrice = existing.currentPrice
        val newPrice = parsed.price
        productDao.update(
            existing.copy(
                title = parsed.title,
                imageUrl = parsed.imageUrl,
                currentPrice = newPrice,
                updatedAt = now,
                lastCheckedAt = now
            )
        )
        historyDao.insert(PriceHistoryEntity(productId = productId, price = newPrice, timestamp = now))
        if (sendNotifications && newPrice < oldPrice) {
            notifications.notifyPriceDrop(productId, parsed.title, oldPrice, newPrice)
        }
        return oldPrice != newPrice
    }

    suspend fun refreshAllProducts(sendNotifications: Boolean = true): Int {
        var updatedCount = 0
        productDao.getProducts().forEach { product ->
            runCatching { refreshProduct(product.id, sendNotifications) }
                .onSuccess { changed -> if (changed) updatedCount += 1 }
                .onFailure { error ->
                    Log.e("ProductRepository", "Failed to refresh product ${product.id}", error)
                }
        }
        return updatedCount
    }

    suspend fun deleteProduct(productId: Long) {
        productDao.delete(productId)
    }
}
