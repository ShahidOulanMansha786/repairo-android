package com.carrepair.app.domain.viewmodels.quotes


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.quote.QuoteResponseDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.stomp.StompClientManager
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class AcceptQuoteUiState {
    object Idle : AcceptQuoteUiState()
    object Loading : AcceptQuoteUiState()
    data class Success(val quoteId: Long, val channelId: String?) : AcceptQuoteUiState()
    data class Error(val message: String) : AcceptQuoteUiState()
}

class QuotesViewModel(
    private val leadApi: LeadApi,
    private val tokenManager: TokenManager,
    private val stompClientManager: StompClientManager
) : ViewModel() {

    private val _quotes = MutableStateFlow<List<QuoteResponseDto>>(emptyList())
    val quotes: StateFlow<List<QuoteResponseDto>> = _quotes

    private val _acceptUiState = MutableStateFlow<AcceptQuoteUiState>(AcceptQuoteUiState.Idle)
    val acceptUiState: StateFlow<AcceptQuoteUiState> = _acceptUiState

    private var stompDisposable: Disposable? = null

    fun loadQuotes(leadId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                val response = leadApi.getQuotesForLead(
                    token = "Bearer $token",
                    leadId = leadId
                )
                if (response.isSuccessful) {
                    _quotes.value = response.body() ?: emptyList()
                }

                stompClientManager.connect(token ?: "")
                stompDisposable = stompClientManager.subscribeToQuotes(leadId) { newQuote ->
                    val current = _quotes.value.toMutableList()
                    val existingIndex = current.indexOfFirst { it.id == newQuote.id }
                    if (existingIndex >= 0) {
                        current[existingIndex] = newQuote
                    } else {
                        current.add(0, newQuote)
                    }
                    _quotes.value = current
                }
            } catch (e: Exception) {
                // silently fail
            }
        }
    }

    fun acceptQuote(leadId: Long, quoteId: Long) {
        viewModelScope.launch {
            _acceptUiState.value = AcceptQuoteUiState.Loading
            try {
                val token = tokenManager.getAccessToken()
                val response = leadApi.acceptQuote(
                    token = "Bearer $token",
                    leadId = leadId,
                    quoteId = quoteId
                )
                if (response.isSuccessful) {
                    val channelId = response.body()?.channelId
                    _acceptUiState.value = AcceptQuoteUiState.Success(quoteId, channelId)
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrBlank()) {
                            JSONObject(errorBody).optString("message", "Failed to accept quote")
                        } else {
                            "Failed to accept quote"
                        }
                    } catch (e: Exception) {
                        "Failed to accept quote"
                    }
                    _acceptUiState.value = AcceptQuoteUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _acceptUiState.value = AcceptQuoteUiState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun resetAcceptState() {
        _acceptUiState.value = AcceptQuoteUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stompDisposable?.dispose()
        stompClientManager.disconnect()
    }
}

class QuotesViewModelFactory(
    private val leadApi: LeadApi,
    private val tokenManager: TokenManager,
    private val stompClientManager: StompClientManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuotesViewModel(leadApi, tokenManager, stompClientManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}