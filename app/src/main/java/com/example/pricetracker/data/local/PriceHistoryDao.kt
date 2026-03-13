package com.example.pricetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PriceHistoryDao {
    @Insert
    suspend fun insert(item: PriceHistoryEntity)

    @Query(
        """
        SELECT * FROM price_history
        WHERE productId = :productId
        ORDER BY checkedAt DESC
        LIMIT 1
        """
    )
    suspend fun latestForProduct(productId: Long): PriceHistoryEntity?
}
