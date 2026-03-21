package com.pricetracker.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pricetracker.app.data.model.PriceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    fun getHistoryForProduct(productId: Long): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    suspend fun getHistoryListForProduct(productId: Long): List<PriceHistory>

    @Insert
    suspend fun insertPriceRecord(priceHistory: PriceHistory)

    @Query("DELETE FROM price_history WHERE productId = :productId")
    suspend fun deleteHistoryForProduct(productId: Long)
}
