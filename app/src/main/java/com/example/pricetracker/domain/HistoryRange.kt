package com.example.pricetracker.domain

enum class HistoryRange(val label: String) {
    DAYS_7("7D"),
    DAYS_30("30D"),
    DAYS_90("90D"),
    ALL("ALL");

    fun toStartTime(now: Long): Long? {
        return when (this) {
            DAYS_7 -> now - 7L * 24 * 60 * 60 * 1000
            DAYS_30 -> now - 30L * 24 * 60 * 60 * 1000
            DAYS_90 -> now - 90L * 24 * 60 * 60 * 1000
            ALL -> null
        }
    }
}
