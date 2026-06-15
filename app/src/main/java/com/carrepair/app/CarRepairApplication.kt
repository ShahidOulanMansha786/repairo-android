package com.carrepair.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.domain.chat.StreamChatManager
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.utils.AppState
import com.carrepair.app.presentation.navigation.AppNavigation
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.screens.payment.PaymentDeepLinkState

class CarRepairApplication : ComponentActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.tokenManager = TokenManager(this)
        StreamChatManager.init(this, BuildConfig.STREAM_API_KEY)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController().also { this.navController = it }
            RepaiiroTheme {
                AppNavigation(navController = navController)
            }

            LaunchedEffect(Unit) {
                handleNavigationIntent(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {

        val notificationType = intent?.getStringExtra("type")
        when (notificationType) {
            "ACCOUNT_BLOCKED" -> {
                RetrofitClient.tokenManager.setBlocked(true)
                AppState.isBlocked.value = true
            }
            "ACCOUNT_UNBLOCKED" -> {
                RetrofitClient.tokenManager.setBlocked(false)
                AppState.isBlocked.value = false
                AppState.shouldNavigateToRoleSelection.value = true
            }
        }

        when (intent?.getStringExtra("navigate_to")) {
            "home" -> navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
            "shop/home" -> navController.navigate("shop/home") {
                popUpTo(0) { inclusive = true }
            }
            "rejected_screen" -> navController.navigate("rejected_screen") {
                popUpTo(0) { inclusive = true }
            }
        }
        // ← ADD THIS BLOCK for payment deep link
        intent?.data?.let { uri ->
            if (uri.scheme == "carrepair" && uri.host == "payment") {
                val paymentId = uri.getQueryParameter("paymentId")?.toLongOrNull() ?: return
                val leadId = PaymentDeepLinkState.pendingLeadId
                if (leadId != 0L) {
                    navController.navigate("payment/success/$paymentId/$leadId") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            }
        }
    }
}

