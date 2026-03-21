package com.example.pricetracker.network

data class ParsedProduct(
    val source: ProductSource,
    val url: String,
    val title: String,
    val imageUrl: String,
    val price: Double
)
