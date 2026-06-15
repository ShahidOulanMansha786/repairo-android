package com.carrepair.app.presentation.screens.shop


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModel
import com.carrepair.app.presentation.screens.Screen


@Composable
fun RejectedScreen(
    navController: NavController,
    authApi: AuthApi,
    tokenManager: TokenManager,
    viewModel: ShopRegistrationViewModel
) {
    var rejectionReason by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler { }

    LaunchedEffect(Unit) {
        try {
            val token = tokenManager.getAccessToken()
            if (token != null) {
                val response = authApi.getMyShopStatus("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    rejectionReason = response.body()!!.rejectionReason
                }
            }
        } catch (e: Exception) {
            // silently ignore — UI will show fallback text
        } finally {
            isLoading = false
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Application Rejected",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = rejectionReason ?: "No reason was provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    tokenManager.clearTokens()
                    viewModel.resetForm()
                    navController.navigate("shop_reg_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Resubmit Application")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    tokenManager.clearTokens()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Logout")
            }
        }
    }
}