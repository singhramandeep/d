package com.example.pricetracker.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pricetracker.data.ProductRepository
import com.example.pricetracker.polling.InAppPollingManager
import com.example.pricetracker.settings.PollingSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ProductRepository,
    private val settingsRepository: PollingSettingsRepository
) : ViewModel() {
    private val pollingManager = InAppPollingManager(repository, settingsRepository, viewModelScope)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        observeSettings()
        pollingManager.start()
    }

    fun onUrlChanged(value: String) {
        _uiState.update { it.copy(urlInput = value) }
    }

    fun addProduct(urlOverride: String? = null) {
        val input = (urlOverride ?: _uiState.value.urlInput).trim()
        if (input.isBlank()) {
            showMessage("Please enter a valid product URL.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.addProduct(input) }
                .onSuccess {
                    _uiState.update { state -> state.copy(urlInput = "", isLoading = false) }
                    showMessage("Product added successfully.")
                }
                .onFailure { error ->
                    Log.e("MainViewModel", "Failed to add product URL", error)
                    _uiState.update { it.copy(isLoading = false) }
                    showMessage(error.message ?: "Unable to add product.")
                }
        }
    }

    fun refreshProduct(productId: Long) {
        viewModelScope.launch {
            runCatching { repository.refreshProduct(productId, sendNotifications = false) }
                .onFailure { error ->
                    Log.e("MainViewModel", "Manual refresh failed for product $productId", error)
                    showMessage(error.message ?: "Refresh failed.")
                }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteProduct(productId) }
                .onFailure { error ->
                    Log.e("MainViewModel", "Failed to delete product $productId", error)
                    showMessage("Delete failed.")
                }
        }
    }

    fun setPollingInterval(minutes: Int) {
        viewModelScope.launch {
            runCatching { settingsRepository.saveInterval(minutes) }
                .onFailure { error ->
                    Log.e("MainViewModel", "Failed to update polling interval", error)
                    showMessage("Could not save polling interval.")
                }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    override fun onCleared() {
        pollingManager.stop()
        super.onCleared()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            repository.observeProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.intervalMinutes.collect { interval ->
                _uiState.update { it.copy(pollingIntervalMinutes = interval) }
            }
        }
    }

    private fun showMessage(value: String) {
        _uiState.update { it.copy(message = value) }
    }

    class Factory(
        private val repository: ProductRepository,
        private val settingsRepository: PollingSettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, settingsRepository) as T
        }
    }
}
