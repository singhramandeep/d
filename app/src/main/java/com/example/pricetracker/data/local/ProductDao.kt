package com.example.pricetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM tracked_products ORDER BY lastCheckedAt DESC")
    fun observeProductsWithHistory(): Flow<List<ProductWithHistory>>

    @Query("SELECT * FROM tracked_products WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): TrackedProductEntity?

    @Query("SELECT * FROM tracked_products")
    suspend fun getAll(): List<TrackedProductEntity>

    @Query("SELECT * FROM tracked_products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TrackedProductEntity?

    @Insert
    suspend fun insert(product: TrackedProductEntity): Long

    @Update
    suspend fun update(product: TrackedProductEntity)

    @Query("DELETE FROM tracked_products WHERE id = :id")
    suspend fun deleteById(id: Long)
}
