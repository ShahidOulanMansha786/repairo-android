package com.carrepair.app.domain.security

import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.dto.auth.RefreshTokenRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.request.header("X-Retry-After-Refresh") != null) return null

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        val newAccessToken = runBlocking {
            try {
                val result = RetrofitClient.authApi.refresh(
                    RefreshTokenRequestDto(refreshToken)
                )
                tokenManager.saveTokens(
                    result.accessToken,
                    result.refreshToken,
                    tokenManager.getRole() ?: ""
                )
                result.accessToken
            } catch (e: Exception) {
                tokenManager.clearTokens()
                null
            }
        }

        return newAccessToken?.let {
            response.request.newBuilder()
                .header("Authorization", "Bearer $it")
                .header("X-Retry-After-Refresh", "true")
                .build()
        }
    }
}