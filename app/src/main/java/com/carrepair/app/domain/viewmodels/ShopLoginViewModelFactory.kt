package com.carrepair.app.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager


class ShopLoginViewModelFactory(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopLoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopLoginViewModel(
                authApi = authApi,
                tokenManager = tokenManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}