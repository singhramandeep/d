package com.pricetracker.app.ui.screens.addproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricetracker.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddProductUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState

    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(url = url, error = null)
    }

    fun addProduct() {
        val url = _uiState.value.url.trim()
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a URL")
            return
        }
        if (!isValidUrl(url)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.addProductFromUrl(url)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to fetch product details"
                    )
                }
            )
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            uri.scheme in listOf("http", "https") && uri.host != null
        } catch (e: Exception) {
            false
        }
    }
}
