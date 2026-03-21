package com.example.pricetracker.ui

import com.example.pricetracker.data.ProductEntity

data class MainUiState(
    val urlInput: String = "",
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false,
    val pollingIntervalMinutes: Int = 5,
    val message: String? = null
)
