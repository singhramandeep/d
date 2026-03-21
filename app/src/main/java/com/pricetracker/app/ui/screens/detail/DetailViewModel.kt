package com.pricetracker.app.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricetracker.app.data.model.PriceHistory
import com.pricetracker.app.data.model.Product
import com.pricetracker.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val product: Product? = null,
    val isRefreshing: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    val priceHistory: StateFlow<List<PriceHistory>> =
        repository.getPriceHistory(productId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            val product = repository.getProductById(productId)
            _uiState.value = _uiState.value.copy(product = product)
        }
    }

    fun refreshPrice() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            repository.refreshPrice(productId).onSuccess { update ->
                _uiState.value = _uiState.value.copy(
                    product = update.product,
                    isRefreshing = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
