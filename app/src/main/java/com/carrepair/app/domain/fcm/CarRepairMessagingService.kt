package com.carrepair.app.domain.fcm


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.carrepair.app.CarRepairApplication
import com.carrepair.app.R
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.utils.AppState
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarRepairMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                applicationContext,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            prefs.edit().putString("pending_fcm_token", token).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val tokenManager = TokenManager(applicationContext)
        if (tokenManager.isLoggedIn()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val accessToken = tokenManager.getAccessToken()
                    RetrofitClient.authApi.updateFcmToken(
                        token = "Bearer $accessToken",
                        body = AuthApi.FcmTokenRequestDto(fcmToken = token)
                    )
                } catch (e: Exception) {
                    // silently fail — pending_fcm_token will be used on next login
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val type = remoteMessage.data["type"]
        val title = remoteMessage.notification?.title ?: ""
        val body = remoteMessage.notification?.body ?: ""

        when (type) {
            "SHOP_APPROVED" -> showNotification(title, body, navigateTo = "home", type = type)
            "SHOP_REJECTED" -> showNotification(title, body, navigateTo = "rejected_screen", type = type)
            "NEW_LEAD" -> showNotification(title, body, navigateTo = "shop/home", type = type)
            "QUOTE_ACCEPTED" -> showNotification(title, body, navigateTo = "shop/home", type = type)
            "QUOTE_REJECTED" -> showNotification(title, body, navigateTo = "shop/home", type = type)
            "NEW_MESSAGE" -> {
                val channelId = remoteMessage.data["channelId"]
                showNotification(
                    title = title,
                    body = body,
                    navigateTo = if (channelId != null) "chat/$channelId" else null,
                    type = type
                )
            }
            "ACCOUNT_BLOCKED" -> {
                RetrofitClient.tokenManager.setBlocked(true)
                AppState.isBlocked.value = true
            }

            "ACCOUNT_UNBLOCKED" -> {
                RetrofitClient.tokenManager.setBlocked(false)
                AppState.isBlocked.value = false
                AppState.shouldNavigateToRoleSelection.value = true
                showNotification(
                    title = remoteMessage.notification?.title ?: "Account Unblocked",
                    body = remoteMessage.notification?.body ?: "Your account has been unblocked.",
                    navigateTo = null,
                    type = "ACCOUNT_UNBLOCKED"
                )
            }
            "SHOP_MARKED_DONE" -> {
                val leadId = remoteMessage.data["leadId"]
                showNotification(
                    title = title,
                    body = body,
                    navigateTo = if (leadId != null) "leads/$leadId" else null,
                    type = type
                )
            }

            "JOB_COMPLETED" -> {
                val leadId = remoteMessage.data["leadId"]
                showNotification(
                    title = title,
                    body = body,
                    navigateTo = if (leadId != null) "leads/$leadId" else null,
                    type = type
                )
            }

            "DISPUTE_RAISED" -> {
                val leadId = remoteMessage.data["leadId"]
                val tokenManager = TokenManager(applicationContext)
                val role = tokenManager.getRole()
                val route = if (role == "SHOP_OWNER") "shop/leads/$leadId" else "leads/$leadId"
                showNotification(title, body, navigateTo = route, type = type)
            }

            "DISPUTE_RESOLVED" -> {
                val leadId = remoteMessage.data["leadId"]
                val tokenManager = TokenManager(applicationContext)
                val role = tokenManager.getRole()
                val route = if (role == "SHOP_OWNER") "shop/leads/$leadId" else "leads/$leadId"
                showNotification(title, body, navigateTo = route, type = type)
            }
            else -> showNotification(title, body, navigateTo = null, type = type)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        navigateTo: String?,
        type: String?
    ) {
        val intent = Intent(this, CarRepairApplication::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (navigateTo != null) {
                putExtra("navigate_to", navigateTo)
            }
            if (type != null) {
                putExtra("type", type)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "car_repair_channel"

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}