package com.example.pricetracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracked_products",
    indices = [Index(value = ["url"], unique = true)]
)
data class TrackedProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val source: String,
    val title: String,
    val currentPricePaise: Long?,
    val targetPricePaise: Long?,
    val lastCheckedAt: Long,
    val lastNotifiedAt: Long? = null
)
