package com.example.pricetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pricetracker.data.PriceRepository
import com.example.pricetracker.data.TrackResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

data class ProductUiModel(
    val id: Long,
    val title: String,
    val url: String,
    val source: String,
    val imageUrl: String?,
    val currentPricePaise: Long?,
    val targetPricePaise: Long?,
    val lastCheckedAt: Long,
    val history: List<Long>
)

data class MainUiState(
    val products: List<ProductUiModel> = emptyList(),
    val pendingSharedUrl: String? = null,
    val message: String? = null,
    val isRefreshingAll: Boolean = false,
    val refreshingProductIds: Set<Long> = emptySet()
)

class MainViewModel(
    private val repository: PriceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.productsWithHistory.collect { items ->
                val mapped = items.map { item ->
                    ProductUiModel(
                        id = item.product.id,
                        title = item.product.title,
                        url = item.product.url,
                        source = item.product.source,
                        imageUrl = item.product.imageUrl,
                        currentPricePaise = item.product.currentPricePaise,
                        targetPricePaise = item.product.targetPricePaise,
                        lastCheckedAt = item.product.lastCheckedAt,
                        history = item.history
                            .sortedBy { it.checkedAt }
                            .map { it.pricePaise }
                    )
                }
                _uiState.update { it.copy(products = mapped) }
            }
        }
    }

    fun onSharedText(sharedText: String?) {
        if (sharedText.isNullOrBlank()) return
        val url = extractFirstUrl(sharedText)
        _uiState.update {
            if (url == null) {
                it.copy(message = "No valid URL found in shared text.")
            } else {
                it.copy(pendingSharedUrl = url)
            }
        }
    }

    fun clearPendingUrl() {
        _uiState.update { it.copy(pendingSharedUrl = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun trackPendingUrl(thresholdRupeesText: String) {
        val pendingUrl = _uiState.value.pendingSharedUrl ?: return
        val target = parseRupeesToPaise(thresholdRupeesText)
        viewModelScope.launch {
            when (val result = repository.trackUrl(pendingUrl, target)) {
                is TrackResult.Success -> {
                    val successMessage = if (result.currentPricePaise == null) {
                        "Tracking added. Price will populate on successful fetch."
                    } else {
                        "Tracking added at ${formatInr(result.currentPricePaise)}."
                    }
                    _uiState.update {
                        it.copy(
                            pendingSharedUrl = null,
                            message = successMessage
                        )
                    }
                }

                TrackResult.InvalidLink -> {
                    _uiState.update { it.copy(message = "Could not track URL. Share a valid product link.") }
                }
            }
        }
    }

    fun refreshAllNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingAll = true, message = null) }
            runCatching { repository.refreshAllPrices() }
                .onSuccess {
                    _uiState.update { it.copy(isRefreshingAll = false, message = "Prices refreshed for all items.") }
                }
                .onFailure {
                    _uiState.update { it.copy(isRefreshingAll = false, message = "Refresh failed. Try again later.") }
                }
        }
    }

    fun refreshProductNow(productId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    refreshingProductIds = it.refreshingProductIds + productId,
                    message = null
                )
            }
            runCatching { repository.refreshProductPrice(productId) }
                .onSuccess { refreshed ->
                    val message = if (refreshed) "Price checked." else "Product not found."
                    _uiState.update {
                        it.copy(
                            refreshingProductIds = it.refreshingProductIds - productId,
                            message = message
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            refreshingProductIds = it.refreshingProductIds - productId,
                            message = "Could not refresh this item."
                        )
                    }
                }
        }
    }

    fun updateTargetPrice(productId: Long, targetRupeesText: String) {
        val target = parseRupeesToPaise(targetRupeesText)
        viewModelScope.launch {
            repository.updateTargetPrice(productId, target)
            _uiState.update { it.copy(message = "Target updated.") }
        }
    }

    fun removeProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _uiState.update { it.copy(message = "Product removed.") }
        }
    }

    fun onAllItemsCopied(count: Int) {
        _uiState.update { it.copy(message = "Copied $count items to clipboard.") }
    }

    private fun extractFirstUrl(text: String): String? {
        val regex = Regex("(https?://[^\\s]+)")
        val raw = regex.find(text)?.groupValues?.getOrNull(1) ?: return null
        return raw.trim().trimEnd(',', '.', ')', ']', '"', '\'')
    }

    private fun parseRupeesToPaise(value: String): Long? {
        if (value.isBlank()) return null
        val decimal = value.trim().toBigDecimalOrNull() ?: return null
        return decimal.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }

    private fun formatInr(paise: Long): String = "₹%.2f".format(paise / 100.0)
}

class MainViewModelFactory(
    private val repository: PriceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
