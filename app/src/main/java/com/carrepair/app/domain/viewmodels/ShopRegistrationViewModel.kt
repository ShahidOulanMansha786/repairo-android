package com.carrepair.app.domain.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.dto.ShopDocumentUploadDto
import com.carrepair.app.data.dto.ShopOtpRequestDto
import com.carrepair.app.data.dto.ShopVerifyOtpRequestDto
import com.carrepair.app.domain.chat.StreamChatManager
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ShopRegistrationFormState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val shopName: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val logoUri: Uri? = null,
    val cnicUri: Uri? = null,
    val businessDocUri: Uri? = null
)

sealed class ShopRegistrationUiState {
    object Idle : ShopRegistrationUiState()
    object Loading : ShopRegistrationUiState()
    data class OtpSent(val email: String) : ShopRegistrationUiState()
    data class Error(val message: String) : ShopRegistrationUiState()
}

sealed class OtpVerificationUiState {
    object Idle : OtpVerificationUiState()
    object Loading : OtpVerificationUiState()
    object Success : OtpVerificationUiState()
    data class Error(val message: String) : OtpVerificationUiState()
}

sealed class DocumentUploadUiState {
    object Idle : DocumentUploadUiState()
    object Loading : DocumentUploadUiState()
    object Success : DocumentUploadUiState()
    data class Error(val message: String) : DocumentUploadUiState()
}

class ShopRegistrationViewModel(
    private val authApi: AuthApi,
    private val s3UploadService: S3UploadService,
    private val tokenManager: TokenManager,
    private val context: Application
) : AndroidViewModel(context) {

    private val _formState = MutableStateFlow(ShopRegistrationFormState())
    val formState: StateFlow<ShopRegistrationFormState> = _formState.asStateFlow()

    private val _registrationUiState = MutableStateFlow<ShopRegistrationUiState>(ShopRegistrationUiState.Idle)
    val registrationUiState: StateFlow<ShopRegistrationUiState> = _registrationUiState.asStateFlow()

    private val _otpUiState = MutableStateFlow<OtpVerificationUiState>(OtpVerificationUiState.Idle)
    val otpUiState: StateFlow<OtpVerificationUiState> = _otpUiState.asStateFlow()

    private val _documentUiState = MutableStateFlow<DocumentUploadUiState>(DocumentUploadUiState.Idle)
    val documentUiState: StateFlow<DocumentUploadUiState> = _documentUiState.asStateFlow()

    private val _resendCooldown = MutableStateFlow(0)
    val resendCooldown: StateFlow<Int> = _resendCooldown.asStateFlow()

    private fun startResendCooldown() {
        viewModelScope.launch {
            for (i in 60 downTo 0) {
                _resendCooldown.value = i
                if (i > 0) delay(1000L)
            }
        }
    }
    fun updateOwnerDetails(fullName: String, email: String, phone: String) {
        _formState.value = _formState.value.copy(
            fullName = fullName,
            email = email,
            phone = phone
        )
    }

    fun updateShopDetails(shopName: String, description: String, address: String, lat: Double, lng: Double) {
        _formState.value = _formState.value.copy(
            shopName = shopName,
            description = description,
            address = address,
            latitude = lat,
            longitude = lng
        )
    }

    fun updateDocumentUri(type: String, uri: Uri) {
        _formState.value = when (type) {
            "logo"     -> _formState.value.copy(logoUri = uri)
            "cnic"     -> _formState.value.copy(cnicUri = uri)
            "business" -> _formState.value.copy(businessDocUri = uri)
            else       -> _formState.value
        }
    }

    fun submitForOtp() {
        val form = _formState.value
        _registrationUiState.value = ShopRegistrationUiState.Loading

        viewModelScope.launch {
            try {
                val response = authApi.requestShopOtp(
                    ShopOtpRequestDto(
                        fullName = form.fullName,
                        email = form.email,
                        phone = form.phone,
                        shopName = form.shopName,
                        description = form.description,
                        address = form.address,
                        latitude = form.latitude,
                        longitude = form.longitude
                    )
                )

                if (response.isSuccessful) {
                    _registrationUiState.value = ShopRegistrationUiState.OtpSent(form.email)
                    startResendCooldown()
                } else {
                    val error = parseErrorMessage(response.errorBody()?.string())
                    _registrationUiState.value = ShopRegistrationUiState.Error(error)
                }
            } catch (e: Exception) {
                _registrationUiState.value = ShopRegistrationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun verifyOtp(otp: String) {
        val email = _formState.value.email
        _otpUiState.value = OtpVerificationUiState.Loading

        viewModelScope.launch {
            try {
                val response = authApi.verifyShopOtp(
                    ShopVerifyOtpRequestDto(email = email, otp = otp)
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken,
                        role = body.role
                    )

                    val bearerToken = "Bearer ${body.accessToken}"

                    try {
                        val meResponse = authApi.getMe(bearerToken)
                        val chatTokenResponse = authApi.getChatToken(bearerToken)

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
                        Log.e("ShopRegistrationViewModel", "Stream connect failed: ${e.message}")
                    }

                    _otpUiState.value = OtpVerificationUiState.Success
                } else {
                    val error = parseErrorMessage(response.errorBody()?.string())
                    _otpUiState.value = OtpVerificationUiState.Error(error)
                }
            } catch (e: Exception) {
                _otpUiState.value = OtpVerificationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun uploadDocuments() {
        val form = _formState.value
        val token = "Bearer " + (tokenManager.getAccessToken() ?: run {
            _documentUiState.value = DocumentUploadUiState.Error("Not authenticated")
            return
        })

        _documentUiState.value = DocumentUploadUiState.Loading

        viewModelScope.launch {
            try {
                val logoKey = form.logoUri?.let {
                    s3UploadService.uploadFileAndGetKey(context, it, "shops/logo", token)
                } ?: throw RuntimeException("Logo is required")

                val cnicKey = form.cnicUri?.let {
                    s3UploadService.uploadFileAndGetKey(context, it, "shops/cnic", token)
                } ?: throw RuntimeException("CNIC document is required")

                val businessDocKey = form.businessDocUri?.let {
                    s3UploadService.uploadFileAndGetKey(context, it, "shops/business", token)
                } ?: throw RuntimeException("Business document is required")

                val response = authApi.uploadShopDocuments(
                    token = token,
                    body = ShopDocumentUploadDto(
                        logoKey = logoKey,
                        cnicKey = cnicKey,
                        businessDocKey = businessDocKey
                    )
                )

                if (response.isSuccessful) {
                    _documentUiState.value = DocumentUploadUiState.Success
                } else {
                    val error = parseErrorMessage(response.errorBody()?.string())
                    _documentUiState.value = DocumentUploadUiState.Error(error)
                }
            } catch (e: Exception) {
                _documentUiState.value = DocumentUploadUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetRegistrationState() {
        _registrationUiState.value = ShopRegistrationUiState.Idle
    }

    fun resetOtpState() {
        _otpUiState.value = OtpVerificationUiState.Idle
    }

    fun resetDocumentState() {
        _documentUiState.value = DocumentUploadUiState.Idle
    }

    fun resetForm() {
        _formState.value = ShopRegistrationFormState()

        _registrationUiState.value = ShopRegistrationUiState.Idle
        _otpUiState.value = OtpVerificationUiState.Idle
        _documentUiState.value = DocumentUploadUiState.Idle
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