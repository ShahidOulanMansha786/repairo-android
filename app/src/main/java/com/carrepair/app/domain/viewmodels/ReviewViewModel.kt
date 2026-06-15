package com.carrepair.app.domain.viewmodels

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.ReviewApi
import com.carrepair.app.data.dto.ReviewResponseDto
import com.carrepair.app.data.dto.SubmitReviewRequestDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class SubmitReviewUiState {
    object Idle : SubmitReviewUiState()
    object Loading : SubmitReviewUiState()
    data class Success(val review: ReviewResponseDto) : SubmitReviewUiState()
    data class Error(val message: String) : SubmitReviewUiState()
}

class ReviewViewModel(
    private val reviewApi: ReviewApi,
    private val s3UploadService: S3UploadService,
    private val tokenManager: TokenManager,
    private val application: Application
) : AndroidViewModel(application) {

    private val _submitState = MutableStateFlow<SubmitReviewUiState>(SubmitReviewUiState.Idle)
    val submitState: StateFlow<SubmitReviewUiState> = _submitState.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewResponseDto>>(emptyList())
    val reviews: StateFlow<List<ReviewResponseDto>> = _reviews.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    private val _reviewsError = MutableStateFlow<String?>(null)
    val reviewsError: StateFlow<String?> = _reviewsError.asStateFlow()

    private val _hasReviewed = MutableStateFlow<Boolean?>(null)
    val hasReviewed: StateFlow<Boolean?> = _hasReviewed.asStateFlow()

    fun loadReviewsForShop(shopId: Long) {
        viewModelScope.launch {
            _isLoadingReviews.value = true
            _reviewsError.value = null
            try {
                val token = "Bearer " + (tokenManager.getAccessToken() ?: "")
                val response = reviewApi.getReviewsForShop(token, shopId)
                if (response.isSuccessful) {
                    _reviews.value = response.body() ?: emptyList()
                } else {
                    _reviewsError.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _reviewsError.value = e.message ?: "Failed to load reviews"
            } finally {
                _isLoadingReviews.value = false
            }
        }
    }

    fun checkIfReviewed(shopId: Long, leadId: Long) {
        viewModelScope.launch {
            try {
                val token = "Bearer " + (tokenManager.getAccessToken() ?: "")
                val response = reviewApi.getReviewsForShop(token, shopId)
                if (response.isSuccessful) {
                    val reviewsList = response.body() ?: emptyList()
                    val exists = reviewsList.any { it.leadId == leadId }
                    _hasReviewed.value = exists
                } else {
                    _hasReviewed.value = false
                }
            } catch (e: Exception) {
                _hasReviewed.value = false
            }
        }
    }

    fun submitReview(
        leadId: Long,
        rating: Int,
        comment: String?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitReviewUiState.Loading
            try {
                val rawToken = tokenManager.getAccessToken() ?: throw RuntimeException("Not authenticated")
                val token = "Bearer $rawToken"

                var uploadedUrl: String? = null
                if (imageUri != null) {
                    uploadedUrl = uploadFileAndGetUrl(imageUri, "reviews/images", rawToken)
                }

                val body = SubmitReviewRequestDto(
                    leadId = leadId,
                    rating = rating,
                    comment = if (comment.isNull_or_blank()) null else comment,
                    imageUrl = uploadedUrl
                )

                val response = reviewApi.submitReview(token, body)
                if (response.isSuccessful && response.body() != null) {
                    _submitState.value = SubmitReviewUiState.Success(response.body()!!)
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    _submitState.value = SubmitReviewUiState.Error(msg)
                }
            } catch (e: Exception) {
                _submitState.value = SubmitReviewUiState.Error(e.message ?: "Failed to submit review")
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitReviewUiState.Idle
    }

    private suspend fun uploadFileAndGetUrl(
        uri: Uri,
        folder: String,
        token: String
    ): String = withContext(Dispatchers.IO) {
        val contentResolver = application.contentResolver

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        val fileName = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        } ?: "review_image_${System.currentTimeMillis()}.jpg"

        val fileBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw RuntimeException("Could not read image bytes")

        val presignedUrlResponse = s3UploadService.getPresignedUrl(
            token = token,
            folder = folder,
            fileName = fileName,
            contentType = mimeType
        )

        val uploaded = s3UploadService.uploadToS3(
            uploadUrl = presignedUrlResponse.uploadUrl,
            fileBytes = fileBytes,
            contentType = mimeType
        )

        if (!uploaded) {
            throw RuntimeException("Failed to upload image")
        }

        presignedUrlResponse.uploadUrl.substringBefore("?")
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    private fun parseError(body: String?): String {
        return try {
            JSONObject(body ?: "{}").optString("message", "Something went wrong")
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}

class ReviewViewModelFactory(
    private val reviewApi: ReviewApi,
    private val s3UploadService: S3UploadService,
    private val tokenManager: TokenManager,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(reviewApi, s3UploadService, tokenManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
