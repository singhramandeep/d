package com.example.pricetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    fun observeProduct(productId: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products")
    suspend fun getProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProduct(productId: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): ProductEntity?

    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun delete(productId: Long)
}
