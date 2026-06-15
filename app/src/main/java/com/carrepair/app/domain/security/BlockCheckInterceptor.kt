package com.carrepair.app.domain.security

import com.carrepair.app.domain.utils.AppState
import okhttp3.Interceptor
import okhttp3.Response

class BlockCheckInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 403) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            if (body.contains("ACCOUNT_BLOCKED")) {
                tokenManager.setBlocked(true)
                AppState.isBlocked.value = true
            }
        }
        return response
    }
}