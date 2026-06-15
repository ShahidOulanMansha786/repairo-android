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
import com.carrepair.app.domain.viewmodels.OtpVerificationUiState
import com.carrepair.app.domain.viewmodels.ShopRegistrationUiState
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModel
import com.carrepair.app.presentation.components.BannerType
import com.carrepair.app.presentation.components.MessageBanner


@Composable
fun ShopOtpScreen(
    viewModel: ShopRegistrationViewModel,
    navController: NavController
) {
    val formState    by viewModel.formState.collectAsState()
    val otpUiState   by viewModel.otpUiState.collectAsState()
    val registrationUiState by viewModel.registrationUiState.collectAsState()
    val resendCooldown by viewModel.resendCooldown.collectAsState()


    var otp          by remember { mutableStateOf("") }
    var bannerMessage by remember { mutableStateOf("") }
    var bannerType    by remember { mutableStateOf<BannerType>(BannerType.Error) }
    var bannerVisible by remember { mutableStateOf(false) }

    val isLoading = otpUiState is OtpVerificationUiState.Loading
    val isResending = registrationUiState is ShopRegistrationUiState.Loading

    LaunchedEffect(otpUiState) {
        when (val state = otpUiState) {
            is OtpVerificationUiState.Success -> {
                navController.navigate("shop_registration/step3") {
                    popUpTo("shop_registration/step1") { inclusive = false }
                }
            }
            is OtpVerificationUiState.Error -> {
                bannerMessage = state.message
                bannerType = BannerType.Error
                bannerVisible = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(registrationUiState) {
        when (val state = registrationUiState) {
            is ShopRegistrationUiState.OtpSent -> {
                bannerMessage = "OTP sent to ${formState.email}"
                bannerType = BannerType.Success
                bannerVisible = true
            }
            is ShopRegistrationUiState.Error -> {
                bannerMessage = state.message
                bannerType = BannerType.Error
                bannerVisible = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.submitForOtp()
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
                    text = "Enter the OTP sent to ${formState.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otp = it },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    TextButton(
                        onClick = { viewModel.submitForOtp() },
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