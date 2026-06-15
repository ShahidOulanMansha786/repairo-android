package com.carrepair.app.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.PaymentApi
import com.carrepair.app.data.dto.payment.PaymentInitiateRequestDto
import com.carrepair.app.data.dto.payment.PaymentSessionResponseDto
import com.carrepair.app.data.dto.payment.PaymentStatusResponseDto
import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// ── UI State ─────────────────────────────────────────────────────────────────

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class SessionCreated(val session: PaymentSessionResponseDto) : PaymentUiState()
    data class StatusLoaded(val status: PaymentStatusResponseDto) : PaymentUiState()
    object Released : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

// ── ViewModel ────────────────────────────────────────────────────────────────

class PaymentViewModel(
    private val paymentApi: PaymentApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState

    private val _paymentStatus = MutableStateFlow<PaymentStatusResponseDto?>(null)
    val paymentStatus: StateFlow<PaymentStatusResponseDto?> = _paymentStatus

    fun initiatePayment(leadId: Long, quoteId: Long) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val response = paymentApi.initiatePayment(
                    token,
                    PaymentInitiateRequestDto(leadId = leadId, quoteId = quoteId)
                )
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = PaymentUiState.SessionCreated(response.body()!!)
                } else {
                    _uiState.value = PaymentUiState.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun loadPaymentStatus(leadId: Long) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val response = paymentApi.getPaymentStatus(token, leadId)
                if (response.isSuccessful && response.body() != null) {
                    val status = response.body()!!
                    _paymentStatus.value = status
                    _uiState.value = PaymentUiState.StatusLoaded(status)
                } else {
                    _uiState.value = PaymentUiState.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun releaseImmediately(paymentId: Long) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val response = paymentApi.releaseImmediately(token, paymentId)
                if (response.isSuccessful) {
                    _uiState.value = PaymentUiState.Released
                } else {
                    _uiState.value = PaymentUiState.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun resetState() { _uiState.value = PaymentUiState.Idle }

    private fun parseError(body: String?): String {
        return try {
            JSONObject(body ?: "{}").optString("message", "Something went wrong")
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}

// ── Factory ──────────────────────────────────────────────────────────────────

class PaymentViewModelFactory(
    private val paymentApi: PaymentApi,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(paymentApi, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
