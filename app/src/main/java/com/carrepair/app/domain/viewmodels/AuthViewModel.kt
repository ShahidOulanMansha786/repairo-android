package com.carrepair.app.domain.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.AuthUiState
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.dto.auth.ApiErrorResponse
import com.carrepair.app.data.dto.auth.LoginRequestDto
import com.carrepair.app.data.dto.auth.ResendOtpRequestDto
import com.carrepair.app.data.dto.auth.SignupRequestDto
import com.carrepair.app.data.dto.auth.VerifyOtpRequestDto
import com.carrepair.app.domain.chat.StreamChatManager
import com.carrepair.app.domain.security.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Yeh sealed class ViewModel file mein add karo
sealed class AuthEvent {
    data class NavigateToOtp(val email: String, val flowType: String) : AuthEvent()
    data class ShowBanner(val message: String, val isSuccess: Boolean) : AuthEvent()
}

class AuthViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    // Single shared UiState for all auth operations
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _resendState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val resendState: StateFlow<AuthUiState> = _resendState.asStateFlow()


    // Called from Sign Up tab
    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun signup(fullName: String, email: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.authApi.signup(
                    SignupRequestDto(fullName = fullName, email = email, phone = phone, role = "CAR_OWNER")
                )
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "OTP sent"
                    _uiState.value = AuthUiState.Idle
                    _events.emit(AuthEvent.ShowBanner(message, isSuccess = true))
                    delay(2000)
                    _events.emit(AuthEvent.NavigateToOtp(email, "signup"))
                } else {
                    _uiState.value = AuthUiState.Idle
                    _events.emit(AuthEvent.ShowBanner(parseErrorMessage(response), isSuccess = false))
                }
            } catch (e: UnknownHostException) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("No internet connection", isSuccess = false))
            } catch (e: SocketTimeoutException) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("Request timed out", isSuccess = false))
            } catch (e: IOException) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("Unable to connect to server", isSuccess = false))
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("Something went wrong", isSuccess = false))
            }
        }
    }

    fun login(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.authApi.login(LoginRequestDto(email = email))
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "OTP sent"
                    _uiState.value = AuthUiState.Idle
                    _events.emit(AuthEvent.ShowBanner(message, isSuccess = true))
                    delay(2000)
                    _events.emit(AuthEvent.NavigateToOtp(email, "login"))
                } else {
                    _uiState.value = AuthUiState.Idle
                    _events.emit(AuthEvent.ShowBanner(parseErrorMessage(response), isSuccess = false))
                }
            } catch (e: UnknownHostException) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("No internet connection", isSuccess = false))
            } catch (e: SocketTimeoutException) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("Request timed out", isSuccess = false))
            } catch (e: IOException) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("Unable to connect to server", isSuccess = false))
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.ShowBanner("Something went wrong", isSuccess = false))
            }
        }
    }

    fun verifyOtp(email: String, otpCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val response = RetrofitClient.authApi.verifyOtp(
                    VerifyOtpRequestDto(email = email, otp = otpCode)
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null) {
                        tokenManager.saveTokens(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken,
                            role = body.role
                        )

                        val bearerToken = "Bearer ${body.accessToken}"

                        try {
                            val meResponse = RetrofitClient.authApi.getMe(bearerToken)
                            val chatTokenResponse = RetrofitClient.authApi.getChatToken(bearerToken)

                            if (meResponse.isSuccessful && chatTokenResponse.isSuccessful) {
                                val me = meResponse.body()
                                val chatToken = chatTokenResponse.body()

                                if (me != null && chatToken != null) {
                                    StreamChatManager.connectUser(
                                        userId = me.id.toString(),
                                        userName = me.fullName,
                                        userToken = chatToken.streamToken
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "Stream connect failed: ${e.message}")
                        }

                        _uiState.value = AuthUiState.Success("OTP verified successfully")
                    } else {
                        _uiState.value = AuthUiState.Error("Empty response from server")
                    }

                } else {
                    _uiState.value = AuthUiState.Error(parseErrorMessage(response))
                }

            } catch (e: UnknownHostException) {
                _uiState.value = AuthUiState.Error("No internet connection")
            } catch (e: SocketTimeoutException) {
                _uiState.value = AuthUiState.Error("Request timed out")
            } catch (e: IOException) {
                _uiState.value = AuthUiState.Error("Unable to connect to server")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Something went wrong")
            }
        }
    }

    // Resets state back to Idle
    // Called after navigating away so stale state does not trigger re-navigation
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun resetResendState() {
        _resendState.value = AuthUiState.Idle
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {

            _resendState.value = AuthUiState.Loading

            try {
                val response = RetrofitClient.authApi.resendOtp(
                    ResendOtpRequestDto(email = email)
                )

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "OTP resent"
                    _resendState.value = AuthUiState.Success(message)
                } else {
                    _resendState.value = AuthUiState.Error(parseErrorMessage(response))
                }

            } catch (e: UnknownHostException) {
                _resendState.value = AuthUiState.Error("No internet connection")

            } catch (e: SocketTimeoutException) {
                _resendState.value = AuthUiState.Error("Request timed out")

            } catch (e: IOException) {
                _resendState.value = AuthUiState.Error("Unable to connect to server")

            } catch (e: Exception) {
                _resendState.value = AuthUiState.Error("Something went wrong")
            }
        }
    }


}

class AuthViewModelFactory(
    private val tokenManager: TokenManager  // factory holds the dependency
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check that the requested ViewModel is actually AuthViewModel
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

            // Cast is safe because we just checked the type above
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

public fun parseErrorMessage(response: Response<*>): String {
    return try {
        // Read the raw JSON string once — stream is consumed after this
        val rawJson = response.errorBody()?.string()

        // If errorBody is null or empty, return a fallback
        if (rawJson.isNullOrEmpty()) {
            return "Something went wrong"
        }

        // Gson parses the raw JSON string into ApiErrorResponse data class
        // fromJson maps JSON keys to Kotlin fields by name
        val errorResponse = Gson().fromJson(rawJson, ApiErrorResponse::class.java)

        // Return only the human-readable message — never expose status codes
        errorResponse.message

    } catch (e: Exception) {
        // If JSON is malformed or Gson fails for any reason, never crash
        // Return a safe fallback message instead
        "Something went wrong"
    }


}