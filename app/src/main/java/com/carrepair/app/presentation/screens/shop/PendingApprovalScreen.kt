package com.carrepair.app.presentation.screens.shop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.fcm.FcmTokenManager
import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.launch

@Composable
fun PendingApprovalScreen(
    navController: NavController,
    authApi: AuthApi,
    tokenManager: TokenManager,
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            FcmTokenManager.getAndRegisterToken(authApi, tokenManager)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your application is under review.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val accessToken = tokenManager.getAccessToken()
                        val response = authApi.getMyShopStatus("Bearer $accessToken")
                        if (response.isSuccessful) {
                            val status = response.body()?.approvalStatus
                            when (status) {
                                "APPROVED" -> navController.navigate("home") {
                                    popUpTo(0) { inclusive = true }
                                }
                                "REJECTED" -> navController.navigate("rejected_screen") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            }) {
                Text("Refresh Status")
            }
        }
    }
}