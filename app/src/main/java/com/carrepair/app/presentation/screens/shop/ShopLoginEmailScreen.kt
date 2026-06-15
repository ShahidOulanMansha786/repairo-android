package com.carrepair.app.presentation.screens.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.ShopLoginEmailUiState
import com.carrepair.app.domain.viewmodels.ShopLoginViewModel
import com.carrepair.app.presentation.components.AuthInputField
import com.carrepair.app.presentation.components.BannerType
import com.carrepair.app.presentation.components.MessageBanner
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextSubtle
import kotlinx.coroutines.launch

@Composable
fun ShopLoginEmailScreen(
    viewModel: ShopLoginViewModel,
    navController: NavController
) {
    val email by viewModel.email.collectAsState()
    val emailUiState by viewModel.emailUiState.collectAsState()

    var bannerMessage by remember { mutableStateOf("") }
    var bannerVisible by remember { mutableStateOf(false) }

    val isLoading = emailUiState is ShopLoginEmailUiState.Loading

    // Shake offset for errors
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(emailUiState) {
        when (emailUiState) {
            is ShopLoginEmailUiState.OtpSent -> {
                navController.navigate("shop_login/otp")
            }
            is ShopLoginEmailUiState.Error -> {
                bannerMessage = (emailUiState as ShopLoginEmailUiState.Error).message
                bannerVisible = true

                // Trigger shake animation
                launch {
                    val shakeSpec = tween<Float>(durationMillis = 80)
                    shakeOffset.animateTo(8f, shakeSpec)
                    shakeOffset.animateTo(-8f, shakeSpec)
                    shakeOffset.animateTo(4f, shakeSpec)
                    shakeOffset.animateTo(-4f, shakeSpec)
                    shakeOffset.animateTo(0f, shakeSpec)
                }
            }
            else -> Unit
        }
    }

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        screenVisible = true
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedVisibility(
                    visible = screenVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Back arrow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = shakeOffset.value.dp)
                        ) {
                            Text(
                                text = "Welcome Back",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter your registered email to sign in",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSubtle
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Custom AuthInputField
                            AuthInputField(
                                value = email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = "Email Address",
                                leadingIcon = Icons.Default.Email,
                                isError = bannerVisible && bannerMessage.isNotEmpty(),
                                errorMessage = if (bannerVisible) bannerMessage else null,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Custom PrimaryButton
                            PrimaryButton(
                                text = "Send OTP",
                                onClick = { viewModel.requestOtp() },
                                enabled = email.isNotBlank(),
                                isLoading = isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Error Message Banner at the bottom
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    MessageBanner(
                        message = bannerMessage,
                        bannerType = BannerType.Error,
                        visible = bannerVisible,
                        onDismiss = {
                            bannerVisible = false
                            viewModel.resetEmailState()
                        }
                    )
                }
            }
        }
    }
}