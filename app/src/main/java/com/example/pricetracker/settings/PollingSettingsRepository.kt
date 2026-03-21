package com.example.pricetracker.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "polling_settings")

class PollingSettingsRepository(private val context: Context) {
    private val intervalKey = intPreferencesKey("polling_interval_minutes")
    private val defaultInterval = 5
    val availableIntervals = listOf(1, 5, 10, 15)

    val intervalMinutes: Flow<Int> = context.dataStore.data
        .catch { error ->
            Log.e("PollingSettings", "Failed to read polling settings", error)
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> sanitizeInterval(prefs[intervalKey]) }

    suspend fun saveInterval(minutes: Int) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[intervalKey] = sanitizeInterval(minutes)
        }
    }

    suspend fun currentInterval(): Int {
        return runCatching { intervalMinutes.first() }.getOrDefault(defaultInterval)
    }

    private fun sanitizeInterval(value: Int?): Int {
        val valid = value ?: defaultInterval
        return if (availableIntervals.contains(valid)) valid else defaultInterval
    }
}
