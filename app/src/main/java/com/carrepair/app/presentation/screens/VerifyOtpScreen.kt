package com.carrepair.app.presentation.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carrepair.app.AuthUiState
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.viewmodels.AuthViewModel
import com.carrepair.app.domain.viewmodels.AuthViewModelFactory
import com.carrepair.app.presentation.components.BannerType
import com.carrepair.app.presentation.components.MessageBanner
import com.carrepair.app.presentation.components.OtpSuccessDialog
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.LightBorder
import com.carrepair.app.presentation.ui.theme.LightSurface
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.OrangeSubtle
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.StatusGreen
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextMuted
import com.carrepair.app.presentation.ui.theme.TextSubtle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyOtpScreen(
    email: String,         // received from navigation argument
    flowType: String,      // received from navigation argument — "signup" or "login"
    onVerified: () -> Unit // callback — called when OTP is verified successfully
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(TokenManager(context))
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var otpCode by remember { mutableStateOf("") }
    var fieldError by remember { mutableStateOf("") }

    // Banner states
    var bannerVisible by remember { mutableStateOf(false) }
    var bannerMessage by remember { mutableStateOf("") }
    var bannerType by remember { mutableStateOf<BannerType>(BannerType.Error) }

    var countdown by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var resendTrigger by remember { mutableIntStateOf(0) }
    val resendState by viewModel.resendState.collectAsStateWithLifecycle()

    var showSuccessDialog by remember { mutableStateOf(false) }

    // Shake offset for errors
    val shakeOffset = remember { Animatable(0f) }

    // Keyboard focus requester
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                // Show success popup directly instead of banner delay
                showSuccessDialog = true
            }
            is AuthUiState.Error -> {
                bannerMessage = (uiState as AuthUiState.Error).message
                bannerType = BannerType.Error
                bannerVisible = true
                fieldError = bannerMessage

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
            else -> {}
        }
    }

    // Countdown timer
    LaunchedEffect(resendTrigger) {
        countdown = 60
        canResend = false
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        canResend = true
    }

    // Staggered box entry animations
    val boxAnims = remember { List(6) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        boxAnims.forEachIndexed { index, anim ->
            launch {
                delay(index * 60L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
        // Auto request focus on text input field
        focusRequester.requestFocus()
    }

    // Filled pulse scale animations for each box
    val boxScales = remember { List(6) { Animatable(1f) } }
    for (i in 0 until 6) {
        val char = otpCode.getOrNull(i)?.toString() ?: ""
        LaunchedEffect(char) {
            if (char.isNotEmpty()) {
                boxScales[i].animateTo(1.08f, tween(75))
                boxScales[i].animateTo(1f, tween(75))
            }
        }
    }

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        screenVisible = true
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    RepaiiroTheme(useDarkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
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
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Back arrow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lock Icon in Orange Circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(color = OrangeSubtle, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Headline
                    Text(
                        text = "Verify Your Email",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle with Email in OrangePrimary
                    val subtitleText = buildAnnotatedString {
                        append("We sent a 6-digit code to ")
                        withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.SemiBold)) {
                            append(email)
                        }
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtle,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 6-digit OTP input boxes row
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = shakeOffset.value.dp)
                    ) {
                        // Hidden BasicTextField to capture inputs
                        BasicTextField(
                            value = otpCode,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    otpCode = input
                                    fieldError = ""
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .focusRequester(focusRequester)
                                .graphicsLayer { alpha = 0.01f }
                        )

                        // Visual boxes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 6) {
                                val char = otpCode.getOrNull(i)?.toString() ?: ""
                                val isFocused = otpCode.length == i || (otpCode.length == 6 && i == 5)

                                // Staged animation properties
                                val scale = boxAnims[i].value * boxScales[i].value
                                val alpha = boxAnims[i].value

                                val isVerified = uiState is AuthUiState.Success
                                val boxBorderColor = when {
                                    isVerified -> StatusGreen
                                    fieldError.isNotEmpty() -> MaterialTheme.colorScheme.error
                                    isFocused || char.isNotEmpty() -> OrangePrimary
                                    else -> LightBorder
                                }

                                val borderWidth = if (isFocused || char.isNotEmpty() || isVerified) 2.dp else 1.dp

                                Box(
                                    modifier = Modifier
                                        .size(width = 52.dp, height = 60.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            this.alpha = alpha
                                        }
                                        .background(
                                            color = LightSurface,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .border(
                                            width = borderWidth,
                                            color = boxBorderColor,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clickable {
                                            focusRequester.requestFocus()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextDark,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Verify Button
                    PrimaryButton(
                        text = "Verify",
                        onClick = {
                            if (otpCode.length < 4) {
                                fieldError = "Please enter a valid OTP"
                                return@PrimaryButton
                            }
                            viewModel.verifyOtp(email = email, otpCode = otpCode)
                        },
                        isLoading = uiState is AuthUiState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resend Code Text
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!canResend) {
                            Text(
                                text = "Resend in 0:${countdown.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    resendTrigger++
                                    viewModel.resendOtp(email)
                                },
                                enabled = resendState !is AuthUiState.Loading
                            ) {
                                if (resendState is AuthUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = OrangePrimary
                                    )
                                } else {
                                    Text(
                                        text = "Resend Code",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Message Banner floating over content at the bottom
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
                        viewModel.resetState()
                    }
                )
            }

            // Success Dialog popup
            OtpSuccessDialog(
                visible = showSuccessDialog,
                onConfirm = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    onVerified()
                }
            )
        }
    }
}