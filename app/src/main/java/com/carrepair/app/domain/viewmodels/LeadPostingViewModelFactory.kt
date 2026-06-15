package com.carrepair.app.domain.viewmodels


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService

class LeadPostingViewModelFactory(
    private val application: Application,
    private val leadApi: LeadApi,
    private val s3UploadService: S3UploadService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadPostingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeadPostingViewModel(
                leadApi = leadApi,
                s3UploadService = s3UploadService,
                tokenManager = tokenManager,
                context = application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}