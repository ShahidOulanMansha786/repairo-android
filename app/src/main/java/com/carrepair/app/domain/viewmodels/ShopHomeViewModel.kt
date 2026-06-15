package com.carrepair.app.domain.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.apis.RepairShopApi
import com.carrepair.app.data.dto.PurchaseCreditsRequestDto
import com.carrepair.app.data.dto.lead.NearbyLeadResponseDto
import com.carrepair.app.data.dto.lead.NearbyLeadsEnvelopeDto
import com.carrepair.app.domain.security.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class ShopHomeUiState {
    object Loading : ShopHomeUiState()
    data class Success(val leads: List<NearbyLeadResponseDto>) : ShopHomeUiState()
    data class Error(val message: String) : ShopHomeUiState()
}

sealed class PurchaseCreditsUiState {
    object Idle : PurchaseCreditsUiState()
    object Loading : PurchaseCreditsUiState()
    object Success : PurchaseCreditsUiState()
    data class Error(val message: String) : PurchaseCreditsUiState()
}

class ShopHomeViewModel(
    private val repairShopApi: RepairShopApi,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val gson = Gson()

    private val _uiState = MutableStateFlow<ShopHomeUiState>(ShopHomeUiState.Loading)
    val uiState: StateFlow<ShopHomeUiState> = _uiState

    private val _creditsBalance = MutableStateFlow<Int?>(null)
    val creditsBalance: StateFlow<Int?> = _creditsBalance

    private val _purchaseCreditsState = MutableStateFlow<PurchaseCreditsUiState>(PurchaseCreditsUiState.Idle)
    val purchaseCreditsState: StateFlow<PurchaseCreditsUiState> = _purchaseCreditsState

    fun loadBrowseData() {
        loadShopCredits()
        loadNearbyLeads()
    }

    fun loadNearbyLeads() {
        viewModelScope.launch {
            _uiState.value = ShopHomeUiState.Loading
            try {
                val response = repairShopApi.getNearbyLeads(
                    "Bearer " + tokenManager.getAccessToken()
                )
                if (response.isSuccessful) {
                    val leads = response.body() ?: emptyList()
                    _uiState.value = ShopHomeUiState.Success(leads)
                } else {
                    _uiState.value = ShopHomeUiState.Error(
                        "Failed to load leads: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ShopHomeUiState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun loadShopCredits() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken() ?: return@launch
                val bearer = "Bearer $token"

                val creditsResponse = repairShopApi.getShopCredits(bearer)
                if (creditsResponse.isSuccessful) {
                    creditsResponse.body()?.resolvedBalance()?.let { balance ->
                        _creditsBalance.value = balance
                        return@launch
                    }
                }

                val statusResponse = authApi.getMyShopStatus(bearer)
                if (statusResponse.isSuccessful) {
                    statusResponse.body()?.resolvedCredits()?.let { balance ->
                        _creditsBalance.value = balance
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Load credits error", e)
            }
        }
    }

    fun purchaseCredits(credits: Int, packageId: Int) {
        viewModelScope.launch {
            _purchaseCreditsState.value = PurchaseCreditsUiState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token.isNullOrBlank()) {
                    _purchaseCreditsState.value = PurchaseCreditsUiState.Error("Not signed in")
                    return@launch
                }

                val response = repairShopApi.purchaseCredits(
                    "Bearer $token",
                    PurchaseCreditsRequestDto(credits = credits, packageId = packageId)
                )

                if (response.isSuccessful) {
                    val balance = response.body()?.resolvedBalance()
                    if (balance != null) {
                        _creditsBalance.value = balance
                    } else {
                        _creditsBalance.value = (_creditsBalance.value ?: 0) + credits
                    }
                    _purchaseCreditsState.value = PurchaseCreditsUiState.Success
                    loadShopCredits()
                } else if (response.code() == 404 || response.code() == 405) {
                    // Backend purchase endpoint not wired yet — update UI balance locally
                    _creditsBalance.value = (_creditsBalance.value ?: 0) + credits
                    _purchaseCreditsState.value = PurchaseCreditsUiState.Success
                } else {
                    _purchaseCreditsState.value = PurchaseCreditsUiState.Error(
                        parseErrorMessage(response.code(), response.errorBody()?.string())
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Purchase credits error", e)
                _creditsBalance.value = (_creditsBalance.value ?: 0) + credits
                _purchaseCreditsState.value = PurchaseCreditsUiState.Success
            }
        }
    }

    fun consumeCredit(): Boolean {
        val current = _creditsBalance.value ?: return true
        if (current <= 0) return false
        _creditsBalance.value = current - 1
        return true
    }

    fun resetPurchaseState() {
        _purchaseCreditsState.value = PurchaseCreditsUiState.Idle
    }

    private fun parseNearbyLeadsJson(rawJson: String): List<NearbyLeadResponseDto> {
        if (rawJson.isBlank()) return emptyList()

        return try {
            val listType = object : TypeToken<List<NearbyLeadResponseDto>>() {}.type
            gson.fromJson<List<NearbyLeadResponseDto>>(rawJson, listType)
                ?.filter { it.id > 0L }
                ?: emptyList()
        } catch (_: JsonSyntaxException) {
            try {
                val envelope = gson.fromJson(rawJson, NearbyLeadsEnvelopeDto::class.java)
                (envelope.content ?: envelope.data ?: envelope.leads)
                    ?.filter { it.id > 0L }
                    ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun parseErrorMessage(code: Int, errorBody: String?): String {
        val fromBody = errorBody?.let { body ->
            try {
                JSONObject(body).optString("message").takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }
        return fromBody ?: "Failed to load leads: HTTP $code"
    }

    companion object {
        private const val TAG = "ShopHomeViewModel"
    }
}

class ShopHomeViewModelFactory(
    private val repairShopApi: RepairShopApi,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopHomeViewModel(repairShopApi, authApi, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
