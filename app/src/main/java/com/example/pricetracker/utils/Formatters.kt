package com.example.pricetracker.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    fun price(value: Double): String = currencyFormat.format(value)

    fun dateTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
}
