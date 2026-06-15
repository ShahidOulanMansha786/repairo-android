package com.carrepair.app.domain.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.RepairShopApi
import com.carrepair.app.data.apis.SubmitQuoteRequestDto

import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class QuoteSubmitUiState {
    object Idle : QuoteSubmitUiState()
    object Loading : QuoteSubmitUiState()
    object Success : QuoteSubmitUiState()
    data class Error(val message: String) : QuoteSubmitUiState()
}

class ShopLeadDetailViewModel(
    private val repairShopApi: RepairShopApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _quoteUiState = MutableStateFlow<QuoteSubmitUiState>(QuoteSubmitUiState.Idle)
    val quoteUiState: StateFlow<QuoteSubmitUiState> = _quoteUiState

    fun submitQuote(leadId: Long, price: String, message: String?) {
        val priceDouble = price.toDoubleOrNull()
        if (priceDouble == null || priceDouble <= 0) {
            _quoteUiState.value = QuoteSubmitUiState.Error("Enter a valid price")
            return
        }

        viewModelScope.launch {
            _quoteUiState.value = QuoteSubmitUiState.Loading
            try {
                val token = tokenManager.getAccessToken()
                val response = repairShopApi.submitQuote(
                    token = "Bearer $token",
                    body = SubmitQuoteRequestDto(
                        leadId = leadId,
                        price = priceDouble,
                        message = message
                    )
                )
                if (response.isSuccessful) {
                    _quoteUiState.value = QuoteSubmitUiState.Success
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrBlank()) {
                            JSONObject(errorBody).optString("message", "Failed to submit quote")
                        } else {
                            "Failed to submit quote"
                        }
                    } catch (e: Exception) {
                        "Failed to submit quote"
                    }
                    _quoteUiState.value = QuoteSubmitUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _quoteUiState.value = QuoteSubmitUiState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun resetState() {
        _quoteUiState.value = QuoteSubmitUiState.Idle
    }
}

class ShopLeadDetailViewModelFactory(
    private val repairShopApi: RepairShopApi,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopLeadDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopLeadDetailViewModel(repairShopApi, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}