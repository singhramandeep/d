package com.example.pricetracker.data

import com.example.pricetracker.data.local.AppDatabase
import com.example.pricetracker.data.local.ProductWithHistory
import com.example.pricetracker.data.local.TrackedProductEntity
import com.example.pricetracker.data.scraper.ProductScraper
import kotlinx.coroutines.flow.Flow
import java.net.URI
import java.util.concurrent.TimeUnit

class PriceRepository(
    private val database: AppDatabase,
    private val scraper: ProductScraper
) {
    val productsWithHistory: Flow<List<ProductWithHistory>> =
        database.productDao().observeProductsWithHistory()

    suspend fun trackUrl(sharedText: String, targetPricePaise: Long?): TrackResult {
        val url = extractFirstUrl(sharedText) ?: return TrackResult.InvalidLink
        val now = System.currentTimeMillis()
        val source = sourceFromUrl(url)

        val productDao = database.productDao()
        val historyDao = database.priceHistoryDao()
        val existing = productDao.getByUrl(url)
        val scraped = scraper.scrape(url)

        val title = scraped?.title ?: existing?.title ?: source
        val imageUrl = scraped?.imageUrl ?: existing?.imageUrl
        val currentPrice = scraped?.pricePaise ?: existing?.currentPricePaise

        val entity = if (existing == null) {
            TrackedProductEntity(
                url = url,
                source = source,
                title = title,
                imageUrl = imageUrl,
                currentPricePaise = currentPrice,
                targetPricePaise = targetPricePaise,
                lastCheckedAt = now
            )
        } else {
            existing.copy(
                source = source,
                title = title,
                imageUrl = imageUrl,
                currentPricePaise = currentPrice,
                targetPricePaise = targetPricePaise ?: existing.targetPricePaise,
                lastCheckedAt = now
            )
        }

        val productId = if (existing == null) {
            productDao.insert(entity)
        } else {
            productDao.update(entity)
            existing.id
        }

        if (currentPrice != null) {
            val latest = historyDao.latestForProduct(productId)
            if (latest == null || latest.pricePaise != currentPrice) {
                historyDao.insert(
                    com.example.pricetracker.data.local.PriceHistoryEntity(
                        productId = productId,
                        pricePaise = currentPrice,
                        checkedAt = now
                    )
                )
            }
        }

        return TrackResult.Success(productId = productId, url = url, currentPricePaise = currentPrice)
    }

    suspend fun updateTargetPrice(productId: Long, targetPricePaise: Long?) {
        val dao = database.productDao()
        val existing = dao.getById(productId) ?: return
        dao.update(existing.copy(targetPricePaise = targetPricePaise))
    }

    suspend fun deleteProduct(productId: Long) {
        database.productDao().deleteById(productId)
    }

    suspend fun refreshAllPrices(
        onThresholdReached: suspend (TrackedProductEntity, Long) -> Unit = { _, _ -> }
    ) {
        val productDao = database.productDao()
        productDao.getAll().forEach { product ->
            refreshProduct(product, onThresholdReached)
        }
    }

    suspend fun refreshProductPrice(
        productId: Long,
        onThresholdReached: suspend (TrackedProductEntity, Long) -> Unit = { _, _ -> }
    ): Boolean {
        val productDao = database.productDao()
        val product = productDao.getById(productId) ?: return false
        refreshProduct(product, onThresholdReached)
        return true
    }

    private fun sourceFromUrl(url: String): String {
        return runCatching { URI(url).host.orEmpty() }
            .getOrDefault("")
            .removePrefix("www.")
            .ifBlank { "unknown" }
    }

    private fun extractFirstUrl(text: String): String? {
        val regex = Regex("(https?://[^\\s]+)")
        val rawUrl = regex.find(text)?.groupValues?.getOrNull(1) ?: return null
        return rawUrl.trim().trimEnd(',', '.', ')', ']', '"', '\'')
    }

    private suspend fun refreshProduct(
        product: TrackedProductEntity,
        onThresholdReached: suspend (TrackedProductEntity, Long) -> Unit
    ) {
        val productDao = database.productDao()
        val historyDao = database.priceHistoryDao()
        val cooldownMillis = TimeUnit.HOURS.toMillis(6)
        val now = System.currentTimeMillis()

        val scraped = scraper.scrape(product.url)
        val newPrice = scraped?.pricePaise ?: product.currentPricePaise
        val newTitle = scraped?.title ?: product.title
        val newImageUrl = scraped?.imageUrl ?: product.imageUrl

        var updated = product.copy(
            title = newTitle,
            imageUrl = newImageUrl,
            currentPricePaise = newPrice,
            lastCheckedAt = now
        )

        if (newPrice != null) {
            val latestHistory = historyDao.latestForProduct(product.id)
            if (latestHistory == null || latestHistory.pricePaise != newPrice) {
                historyDao.insert(
                    com.example.pricetracker.data.local.PriceHistoryEntity(
                        productId = product.id,
                        pricePaise = newPrice,
                        checkedAt = now
                    )
                )
            }
        }

        val targetPrice = updated.targetPricePaise
        val lastNotifiedAt = updated.lastNotifiedAt
        val isThresholdReached = newPrice != null &&
            targetPrice != null &&
            newPrice <= targetPrice
        val canNotify = lastNotifiedAt == null || (now - lastNotifiedAt) >= cooldownMillis
        if (isThresholdReached && canNotify) {
            updated = updated.copy(lastNotifiedAt = now)
            onThresholdReached(updated, newPrice!!)
        }

        productDao.update(updated)
    }
}

sealed interface TrackResult {
    data class Success(
        val productId: Long,
        val url: String,
        val currentPricePaise: Long?
    ) : TrackResult

    data object InvalidLink : TrackResult
}
