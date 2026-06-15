package com.carrepair.app.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carrepair.app.AuthUiState
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.utils.validateSignupFields
import com.carrepair.app.domain.viewmodels.AuthEvent
import com.carrepair.app.domain.viewmodels.AuthViewModel
import com.carrepair.app.domain.viewmodels.AuthViewModelFactory
import com.carrepair.app.presentation.components.AuthInputField
import com.carrepair.app.presentation.components.BannerType
import com.carrepair.app.presentation.components.MessageBanner
import com.carrepair.app.presentation.components.OtpSuccessDialog
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextSubtle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CarOwnerAuthScreen(navController: NavController) {
    // 0 = Sign Up tab, 1 = Log In tab
    var selectedTabIndex by remember { mutableStateOf(1) } // Default to Login

    // Sign Up form fields
    var fullName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Log In form field
    var loginEmail by remember { mutableStateOf("") }

    // Validation error messages — null means no error
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var signupEmailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var loginEmailError by remember { mutableStateOf<String?>(null) }



    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(TokenManager(context))
    )

    // Controls whether the banner is currently visible
    var bannerVisible by remember { mutableStateOf(false) }
    var bannerMessage by remember { mutableStateOf("") }
    var bannerType by remember { mutableStateOf<BannerType>(BannerType.Error) }

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Shake offset for errors
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.ShowBanner -> {
                    bannerMessage = event.message
                    bannerType = if (event.isSuccess) BannerType.Success else BannerType.Error
                    bannerVisible = true

                    // Error par shake
                    if (!event.isSuccess) {
                        launch {
                            val shakeSpec = tween<Float>(durationMillis = 80)
                            shakeOffset.animateTo(8f, shakeSpec)
                            shakeOffset.animateTo(-8f, shakeSpec)
                            shakeOffset.animateTo(4f, shakeSpec)
                            shakeOffset.animateTo(-4f, shakeSpec)
                            shakeOffset.animateTo(0f, shakeSpec)
                        }
                    }
                }

                is AuthEvent.NavigateToOtp -> {
                    navController.navigate(
                        Screen.VerifyOtp.createRoute(event.email, event.flowType)
                    )
                }
            }
        }
    }

//    LaunchedEffect(uiState) {
//        when (uiState) {
//            is AuthUiState.Success -> {
//                bannerMessage = (uiState as AuthUiState.Success).message
//                bannerType = BannerType.Success
//                bannerVisible = true
//
//                // Wait 2 seconds then navigate to OTP screen
//                delay(2000)
//
//                val email = when (selectedTabIndex) {
//                    0 -> signupEmail
//                    else -> loginEmail
//                }
//                val flowType = when (selectedTabIndex) {
//                    0 -> "signup"
//                    else -> "login"
//                }
//
//                viewModel.resetState()
//                navController.navigate(Screen.VerifyOtp.createRoute(email, flowType))
//            }
//
//            is AuthUiState.Error -> {
//                bannerMessage = (uiState as AuthUiState.Error).message
//                bannerType = BannerType.Error
//                bannerVisible = true
//
//                // Trigger shake animation
//                launch {
//                    val shakeSpec = tween<Float>(durationMillis = 80)
//                    shakeOffset.animateTo(8f, shakeSpec)
//                    shakeOffset.animateTo(-8f, shakeSpec)
//                    shakeOffset.animateTo(4f, shakeSpec)
//                    shakeOffset.animateTo(-4f, shakeSpec)
//                    shakeOffset.animateTo(0f, shakeSpec)
//                }
//            }
//
//            else -> {}
//        }
//    }

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        screenVisible = true
    }

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
                        .padding(horizontal = 16.dp) // ScreenPadding
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Back arrow and screen title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Toggle (Animated Switching between Login and Signup)
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "AuthModeSwitch"
                    ) { tabIndex ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = shakeOffset.value.dp)
                        ) {
                            if (tabIndex == 1) {
                                // SIGN IN MODE
                                Text(
                                    text = "Welcome Back",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sign in to your car owner account",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSubtle
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                // Inputs
                                AuthInputField(
                                    value = loginEmail,
                                    onValueChange = {
                                        loginEmail = it
                                        loginEmailError = null
                                    },
                                    label = "Email Address",
                                    leadingIcon = Icons.Default.Email,
                                    isError = loginEmailError != null,
                                    errorMessage = loginEmailError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Sign In Primary button
                                PrimaryButton(
                                    text = "Sign In",
                                    onClick = {
                                        var isValid = true
                                        if (loginEmail.isBlank()) {
                                            loginEmailError = "Email is required"
                                            isValid = false
                                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
                                            loginEmailError = "Enter a valid email"
                                            isValid = false
                                        }
                                        if (isValid) {
                                            viewModel.login(loginEmail.trim())
                                        }
                                    },
                                    isLoading = uiState is AuthUiState.Loading,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Switch to Signup text link
                                val signUpText = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = TextSubtle)) {
                                        append("Don't have an account? ")
                                    }
                                    withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.SemiBold)) {
                                        append("Sign Up")
                                    }
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = signUpText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.clickable {
                                            selectedTabIndex = 0
                                            loginEmailError = null
                                        }
                                    )
                                }
                            } else {
                                // CREATE ACCOUNT MODE
                                Text(
                                    text = "Create Account",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sign up as a car owner",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSubtle
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                // Inputs
                                AuthInputField(
                                    value = fullName,
                                    onValueChange = {
                                        fullName = it
                                        fullNameError = null
                                    },
                                    label = "Full Name",
                                    leadingIcon = Icons.Default.Person,
                                    isError = fullNameError != null,
                                    errorMessage = fullNameError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AuthInputField(
                                    value = signupEmail,
                                    onValueChange = {
                                        signupEmail = it
                                        signupEmailError = null
                                    },
                                    label = "Email Address",
                                    leadingIcon = Icons.Default.Email,
                                    isError = signupEmailError != null,
                                    errorMessage = signupEmailError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AuthInputField(
                                    value = phone,
                                    onValueChange = {
                                        phone = it
                                        phoneError = null
                                    },
                                    label = "Phone Number",
                                    leadingIcon = Icons.Default.Phone,
                                    isError = phoneError != null,
                                    errorMessage = phoneError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Create Account Primary button
                                PrimaryButton(
                                    text = "Create Account",
                                    onClick = {
                                        val (nameErr, emailErr, phoneErr) = validateSignupFields(
                                            fullName = fullName,
                                            email = signupEmail,
                                            phone = phone
                                        )
                                        fullNameError = nameErr
                                        signupEmailError = emailErr
                                        phoneError = phoneErr

                                        if (nameErr == null && emailErr == null && phoneErr == null) {
                                            viewModel.signup(
                                                fullName = fullName.trim(),
                                                email = signupEmail.trim(),
                                                phone = phone.trim()
                                            )
                                        }
                                    },
                                    isLoading = uiState is AuthUiState.Loading,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Switch to Login text link
                                val signInText = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = TextSubtle)) {
                                        append("Already have an account? ")
                                    }
                                    withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.SemiBold)) {
                                        append("Sign In")
                                    }
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = signInText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.clickable {
                                            selectedTabIndex = 1
                                            fullNameError = null
                                            signupEmailError = null
                                            phoneError = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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
        }
    }
}