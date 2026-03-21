package com.example.pricetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert
    suspend fun insert(entry: PriceHistoryEntity)

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    fun observeHistory(productId: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE productId = :productId AND timestamp >= :fromTime ORDER BY timestamp ASC")
    fun observeHistoryFrom(productId: Long, fromTime: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(productId: Long): PriceHistoryEntity?
}
