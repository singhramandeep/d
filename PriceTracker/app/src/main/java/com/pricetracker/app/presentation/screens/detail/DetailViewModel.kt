package com.pricetracker.app.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricetracker.app.data.repository.ProductRepository
import com.pricetracker.app.domain.model.PriceHistory
import com.pricetracker.app.domain.model.Product
import com.pricetracker.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val product: Product? = null,
    val priceHistory: List<PriceHistory> = emptyList(),
    val lowestPrice: Double? = null,
    val highestPrice: Double? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
        loadPriceHistory()
        loadPriceStats()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            repository.getProductById(productId).collect { product ->
                _uiState.update { it.copy(product = product) }
            }
        }
    }

    private fun loadPriceHistory() {
        viewModelScope.launch {
            repository.getPriceHistory(productId).collect { history ->
                _uiState.update { it.copy(priceHistory = history) }
            }
        }
    }

    private fun loadPriceStats() {
        viewModelScope.launch {
            val lowest = repository.getLowestPrice(productId)
            val highest = repository.getHighestPrice(productId)
            _uiState.update { it.copy(lowestPrice = lowest, highestPrice = highest) }
        }
    }

    fun refreshPrice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            when (val result = repository.refreshProduct(productId)) {
                is Result.Success -> {
                    loadPriceStats()
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isRefreshing = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
