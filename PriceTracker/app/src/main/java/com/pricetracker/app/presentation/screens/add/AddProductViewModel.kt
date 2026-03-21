package com.pricetracker.app.presentation.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricetracker.app.data.repository.ProductRepository
import com.pricetracker.app.domain.model.Product
import com.pricetracker.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddProductUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val addedProduct: Product? = null
)

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun updateUrl(url: String) {
        _uiState.update { it.copy(url = url, error = null) }
    }

    fun addProduct() {
        val url = _uiState.value.url.trim()
        if (url.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a product URL") }
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _uiState.update { it.copy(error = "Please enter a valid URL starting with http:// or https://") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.addProduct(url)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, addedProduct = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun setSharedUrl(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.update { AddProductUiState() }
    }
}
