package com.pricetracker.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    fun getPriceHistory(productId: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    suspend fun getPriceHistoryOnce(productId: Long): List<PriceHistoryEntity>

    @Insert
    suspend fun insertPriceHistory(history: PriceHistoryEntity)

    @Query("DELETE FROM price_history WHERE productId = :productId")
    suspend fun deletePriceHistory(productId: Long)

    @Query("SELECT MIN(price) FROM price_history WHERE productId = :productId")
    suspend fun getLowestPrice(productId: Long): Double?

    @Query("SELECT MAX(price) FROM price_history WHERE productId = :productId")
    suspend fun getHighestPrice(productId: Long): Double?
}
