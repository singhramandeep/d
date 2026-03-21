package com.example.pricetracker.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pricetracker.data.PriceHistoryEntity
import com.example.pricetracker.data.ProductEntity
import com.example.pricetracker.data.ProductRepository
import com.example.pricetracker.domain.HistoryRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: ProductEntity? = null,
    val history: List<PriceHistoryEntity> = emptyList(),
    val range: HistoryRange = HistoryRange.DAYS_30
)

class ProductDetailViewModel(
    private val productId: Long,
    private val repository: ProductRepository
) : ViewModel() {
    private val selectedRange = MutableStateFlow(HistoryRange.DAYS_30)
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        observeProduct()
        observeHistory()
    }

    fun selectRange(range: HistoryRange) {
        selectedRange.value = range
        _uiState.update { it.copy(range = range) }
    }

    private fun observeProduct() {
        viewModelScope.launch {
            repository.observeProduct(productId).collect { product ->
                _uiState.update { it.copy(product = product) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHistory() {
        viewModelScope.launch {
            selectedRange.flatMapLatest { range ->
                repository.observeHistory(productId, range)
            }.collect { history ->
                _uiState.update { it.copy(history = history, range = selectedRange.value) }
            }
        }
    }

    class Factory(
        private val productId: Long,
        private val repository: ProductRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductDetailViewModel(productId, repository) as T
        }
    }
}
