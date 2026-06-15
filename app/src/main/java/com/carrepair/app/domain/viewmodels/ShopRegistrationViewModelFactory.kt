package com.carrepair.app.domain.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService


class ShopRegistrationViewModelFactory(
    private val application: Application,
    private val authApi: AuthApi,
    private val s3UploadService: S3UploadService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopRegistrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopRegistrationViewModel(
                authApi = authApi,
                s3UploadService = s3UploadService,
                tokenManager = tokenManager,
                context = application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}