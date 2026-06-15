package com.carrepair.app.presentation.screens.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.ShopLoginEmailUiState
import com.carrepair.app.domain.viewmodels.ShopLoginOtpUiState
import com.carrepair.app.domain.viewmodels.ShopLoginViewModel
import com.carrepair.app.presentation.components.BannerType
import com.carrepair.app.presentation.components.MessageBanner
import com.carrepair.app.presentation.screens.Screen

@Composable
fun ShopLoginOtpScreen(
    viewModel: ShopLoginViewModel,
    navController: NavController
) {
    val email by viewModel.email.collectAsState()
    val otpUiState by viewModel.otpUiState.collectAsState()
    val emailUiState by viewModel.emailUiState.collectAsState()
    val resendCooldown by viewModel.resendCooldown.collectAsState()

    var otp by remember { mutableStateOf("") }
    var bannerMessage by remember { mutableStateOf("") }
    var bannerType by remember { mutableStateOf<BannerType>(BannerType.Error) }
    var bannerVisible by remember { mutableStateOf(false) }

    val isLoading = otpUiState is ShopLoginOtpUiState.Loading
    val isResending = emailUiState is ShopLoginEmailUiState.Loading

    LaunchedEffect(otpUiState) {
        when (val state = otpUiState) {
            is ShopLoginOtpUiState.Success -> {
                when (state.approvalStatus) {
                    "INCOMPLETE" -> navController.navigate("shop_registration/step3") {
                        popUpTo(0) { inclusive = true }
                    }
                    "PENDING" -> navController.navigate("pending_approval") {
                        popUpTo(0) { inclusive = true }
                    }
                    "APPROVED" -> navController.navigate("shop/home") {
                        popUpTo(0) { inclusive = true }
                    }
                    "REJECTED" -> navController.navigate("rejected_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                    else -> navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is ShopLoginOtpUiState.Error -> {
                bannerMessage = state.message
                bannerType = BannerType.Error
                bannerVisible = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(emailUiState) {
        when (emailUiState) {
            is ShopLoginEmailUiState.OtpSent -> {
                bannerMessage = "OTP resent to $email"
                bannerType = BannerType.Success
                bannerVisible = true
            }
            is ShopLoginEmailUiState.Error -> {
                bannerMessage = (emailUiState as ShopLoginEmailUiState.Error).message
                bannerType = BannerType.Error
                bannerVisible = true
            }
            else -> Unit
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Enter the OTP sent to $email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = otp,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) otp = it
                    },
                    label = { Text("OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.verifyOtp(otp) },
                    enabled = otp.length == 6 && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Verify")
                    }
                }

                if (resendCooldown > 0) {
                    Text(
                        text = "Resend OTP in ${resendCooldown}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    TextButton(
                        onClick = { viewModel.resendOtp() },
                        enabled = !isResending,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isResending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Resend OTP")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                MessageBanner(
                    message = bannerMessage,
                    bannerType = bannerType,
                    visible = bannerVisible,
                    onDismiss = {
                        bannerVisible = false
                        viewModel.resetOtpState()
                    }
                )
            }
        }
    }
}