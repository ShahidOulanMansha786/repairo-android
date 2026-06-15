package com.carrepair.app.domain.viewmodels.quotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.RepairShopApi
import com.carrepair.app.data.dto.quote.ShopQuoteResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopQuotesViewModel(private val api: RepairShopApi) : ViewModel() {

    private val _quotes = MutableStateFlow<List<ShopQuoteResponse>>(emptyList())
    val quotes: StateFlow<List<ShopQuoteResponse>> = _quotes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var selectedFilter by mutableStateOf("All")
        private set

    fun loadQuotes(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _quotes.value = api.getMyQuotes(status)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilter(filter: String) {
        selectedFilter = filter
        val status = when (filter) {
            "Pending" -> "PENDING"
            "Accepted" -> "ACCEPTED"
            "Rejected" -> "REJECTED"
            else -> null
        }
        loadQuotes(status)
    }
}

class ShopQuotesViewModelFactory(private val api: RepairShopApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShopQuotesViewModel(api) as T
    }
}