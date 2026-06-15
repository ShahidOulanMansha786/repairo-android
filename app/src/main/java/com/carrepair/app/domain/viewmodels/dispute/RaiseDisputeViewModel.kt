package com.carrepair.app.domain.viewmodels.dispute

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.DisputeApi
import com.carrepair.app.data.dto.dispute.RaiseDisputeRequestDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService
import kotlinx.coroutines.launch
import kotlin.collections.filter

class RaiseDisputeViewModel(
    private val disputeApi: DisputeApi,
    private val tokenManager: TokenManager,
    private val s3UploadService: S3UploadService,
    private val context: Application
) : AndroidViewModel(context) {

    var reason by mutableStateOf("")
    var imageUris by mutableStateOf<List<Uri>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)

    fun addImage(uri: Uri) {
        if (imageUris.size < 5) imageUris = imageUris + uri
    }

    fun removeImage(uri: Uri) {
        imageUris = imageUris.filter { it != uri }
    }

    fun submitDispute(leadId: Long) {
        if (reason.isBlank()) {
            error = "Reason is required"
            return
        }
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val token = "Bearer " + tokenManager.getAccessToken()

                val imageKeys = mutableListOf<String>()
                for (uri in imageUris) {
                    val key = s3UploadService.uploadFileAndGetKey(
                        context, uri, "disputes/images", token
                    )
                    imageKeys.add(key)
                }

                val imageUrlsString = imageKeys.joinToString(",")

                val response = disputeApi.raiseDispute(
                    token, leadId,
                    RaiseDisputeRequestDto(reason = reason, imageUrls = imageUrlsString)
                )
                if (response.isSuccessful) {
                    success = true
                } else {
                    error = "Failed to submit dispute"
                }
            } catch (e: Exception) {
                error = e.message ?: "Error"
            } finally {
                isLoading = false
            }
        }
    }
}

class RaiseDisputeViewModelFactory(
    private val disputeApi: DisputeApi,
    private val tokenManager: TokenManager,
    private val s3UploadService: S3UploadService,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RaiseDisputeViewModel(disputeApi, tokenManager, s3UploadService, application) as T
    }
}