package com.carrepair.app.domain.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()



    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_BLOCKED = "is_blocked"

    }

    fun saveTokens(accessToken: String, refreshToken: String, role: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    public fun isLoggedIn(): Boolean = getAccessToken() != null

    fun setBlocked(blocked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_BLOCKED, blocked).apply()
    }

    fun isUserBlocked(): Boolean = prefs.getBoolean(KEY_IS_BLOCKED, false)
}