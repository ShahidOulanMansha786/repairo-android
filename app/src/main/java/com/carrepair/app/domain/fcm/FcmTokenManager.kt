package com.carrepair.app.domain.fcm


import android.util.Log
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FcmTokenManager {

    private const val TAG = "FcmTokenManager"

    suspend fun getAndRegisterToken(
        authApi: AuthApi,
        tokenManager: TokenManager
    ) {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM token retrieved: $fcmToken")

            val accessToken = tokenManager.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "No access token found, skipping FCM registration")
                return
            }

            val response = authApi.updateFcmToken(
                token = "Bearer $accessToken",
                body = AuthApi.FcmTokenRequestDto(fcmToken = fcmToken)
            )

            if (response.isSuccessful) {
                Log.d(TAG, "FCM token registered successfully")
            } else {
                Log.e(TAG, "FCM token registration failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "FCM token registration error: ${e.message}")
        }
    }
}