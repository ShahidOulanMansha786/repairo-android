package com.carrepair.app.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.dto.ShopLoginRequestDto
import com.carrepair.app.data.dto.ShopVerifyOtpRequestDto
import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ShopLoginEmailUiState {
    object Idle : ShopLoginEmailUiState()
    object Loading : ShopLoginEmailUiState()
    data class OtpSent(val email: String) : ShopLoginEmailUiState()
    data class Error(val message: String) : ShopLoginEmailUiState()
}

sealed class ShopLoginOtpUiState {
    object Idle : ShopLoginOtpUiState()
    object Loading : ShopLoginOtpUiState()
    data class Success(
        val approvalStatus: String,
        val rejectionReason: String?
    ) : ShopLoginOtpUiState()
    data class Error(val message: String) : ShopLoginOtpUiState()
}

class ShopLoginViewModel(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _emailUiState = MutableStateFlow<ShopLoginEmailUiState>(ShopLoginEmailUiState.Idle)
    val emailUiState: StateFlow<ShopLoginEmailUiState> = _emailUiState.asStateFlow()

    private val _otpUiState = MutableStateFlow<ShopLoginOtpUiState>(ShopLoginOtpUiState.Idle)
    val otpUiState: StateFlow<ShopLoginOtpUiState> = _otpUiState.asStateFlow()

    private val _resendCooldown = MutableStateFlow(0)
    val resendCooldown: StateFlow<Int> = _resendCooldown.asStateFlow()

    fun updateEmail(value: String) {
        _email.value = value
    }

    fun requestOtp() {
        val currentEmail = _email.value.trim()
        if (currentEmail.isBlank()) return

        _emailUiState.value = ShopLoginEmailUiState.Loading

        viewModelScope.launch {
            try {
                val response = authApi.shopLoginRequestOtp(
                    ShopLoginRequestDto(email = currentEmail)
                )

                if (response.isSuccessful) {
                    _emailUiState.value = ShopLoginEmailUiState.OtpSent(currentEmail)
                    startResendCooldown()
                } else {
                    val error = parseErrorMessage(response.errorBody()?.string())
                    _emailUiState.value = ShopLoginEmailUiState.Error(error)
                }
            } catch (e: Exception) {
                _emailUiState.value = ShopLoginEmailUiState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun resendOtp() {
        _emailUiState.value = ShopLoginEmailUiState.Idle
        requestOtp()
    }

    fun verifyOtp(otp: String) {
        val currentEmail = _email.value.trim()
        _otpUiState.value = ShopLoginOtpUiState.Loading

        viewModelScope.launch {
            try {
                val response = authApi.shopLoginVerifyOtp(
                    ShopVerifyOtpRequestDto(email = currentEmail, otp = otp)
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken,
                        role = body.role
                    )
                    _otpUiState.value = ShopLoginOtpUiState.Success(
                        approvalStatus = body.approvalStatus,
                        rejectionReason = body.rejectionReason
                    )
                } else {
                    val error = parseErrorMessage(response.errorBody()?.string())
                    _otpUiState.value = ShopLoginOtpUiState.Error(error)
                }
            } catch (e: Exception) {
                _otpUiState.value = ShopLoginOtpUiState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun resetOtpState() {
        _otpUiState.value = ShopLoginOtpUiState.Idle
    }

    fun resetEmailState() {
        _emailUiState.value = ShopLoginEmailUiState.Idle
    }

    private fun startResendCooldown() {
        viewModelScope.launch {
            for (i in 60 downTo 0) {
                _resendCooldown.value = i
                if (i > 0) delay(1000L)
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody == null) return "Something went wrong"
        return try {
            val json = org.json.JSONObject(errorBody)
            json.getString("message").trimEnd('.')
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}