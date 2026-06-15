package com.carrepair.app.domain.viewmodels

import android.net.Uri
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.lead.CreateLeadRequestDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class LeadFormState(
    val title: String = "",
    val description: String = "",
    val carMake: String = "",
    val carModel: String = "",
    val carYear: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUris: List<Uri> = emptyList(),
    val imageKeys: List<String> = emptyList(),
    val category: String = ""
)

sealed class LeadPostingUiState {
    object Idle : LeadPostingUiState()
    object Loading : LeadPostingUiState()
    data class Success(val leadId: Long) : LeadPostingUiState()
    data class Error(val message: String) : LeadPostingUiState()
}


class LeadPostingViewModel(
    private val leadApi: LeadApi,
    private val s3UploadService: S3UploadService,
    private val tokenManager: TokenManager,
    private val context: Application
) : AndroidViewModel(context) {

    private val _formState = MutableStateFlow(LeadFormState())
    val formState: StateFlow<LeadFormState> = _formState

    private val _uiState = MutableStateFlow<LeadPostingUiState>(LeadPostingUiState.Idle)
    val uiState: StateFlow<LeadPostingUiState> = _uiState

    fun updateCarDetails(
        title: String,
        carMake: String,
        carModel: String,
        carYear: String
    ) {
        _formState.value = _formState.value.copy(
            title = title,
            carMake = carMake,
            carModel = carModel,
            carYear = carYear
        )
    }

    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }

    fun updateCategory(category: String) {
        _formState.value = _formState.value.copy(category = category)
    }

    fun addImageUri(uri: Uri) {
        _formState.value = _formState.value.copy(
            imageUris = _formState.value.imageUris + uri
        )
    }

    fun removeImageUri(uri: Uri) {
        _formState.value = _formState.value.copy(
            imageUris = _formState.value.imageUris.filter { it != uri }
        )
    }

    fun updateLocation(address: String, lat: Double, lng: Double) {
        _formState.value = _formState.value.copy(
            address = address,
            latitude = lat,
            longitude = lng
        )
    }

    fun submitLead() {
        viewModelScope.launch {
            _uiState.value = LeadPostingUiState.Loading
            try {
                val token = "Bearer " + tokenManager.getAccessToken()
                val form = _formState.value

                val imageKeys = mutableListOf<String>()
                for (uri in form.imageUris) {
                    val key = s3UploadService.uploadFileAndGetKey(
                        context, uri, "leads/images", token
                    )
                    imageKeys.add(key)
                }

                val dto = CreateLeadRequestDto(
                    title = form.title,
                    description = form.description,
                    carMake = form.carMake,
                    carModel = form.carModel,
                    carYear = form.carYear.toIntOrNull() ?: 0,
                    address = form.address,
                    latitude = form.latitude,
                    longitude = form.longitude,
                    imageKeys = imageKeys
                )

                val response = leadApi.createLead(token, dto)

                if (response.isSuccessful) {
                    val lead = response.body()!!
                    _uiState.value = LeadPostingUiState.Success(lead.id)
                } else {
                    val message = response.errorBody()?.string()?.let {
                        runCatching { JSONObject(it).getString("message") }.getOrNull()
                    } ?: "Something went wrong"
                    _uiState.value = LeadPostingUiState.Error(message)
                }

            } catch (e: Exception) {
                _uiState.value = LeadPostingUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }
}