package com.example.pricetracker.polling

import android.util.Log
import com.example.pricetracker.data.ProductRepository
import com.example.pricetracker.settings.PollingSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class InAppPollingManager(
    private val repository: ProductRepository,
    private val settingsRepository: PollingSettingsRepository,
    private val scope: CoroutineScope
) {
    private var pollingJob: Job? = null

    fun start() {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                runCatching { repository.refreshAllProducts(sendNotifications = true) }
                    .onFailure { error -> Log.e("InAppPolling", "Polling cycle failed", error) }
                val delayMinutes = settingsRepository.currentInterval().toLong()
                delay(delayMinutes * 60_000L)
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
