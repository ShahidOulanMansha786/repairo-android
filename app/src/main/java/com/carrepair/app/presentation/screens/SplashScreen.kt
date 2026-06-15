package com.carrepair.app.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.carrepair.app.R
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.ui.theme.NavyDark
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    LaunchedEffect(Unit) {
        // Delay for 1.2 seconds before proceeding with navigation checks
        delay(1200)

        val isLoggedIn = tokenManager.isLoggedIn()

        if (!isLoggedIn) {
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }
            return@LaunchedEffect
        }

        val role = tokenManager.getRole()
        val token = tokenManager.getAccessToken()

        if (role == "SHOP_OWNER" && token != null) {
            try {
                val authApi = RetrofitClient.authApi
                val response = authApi.getMyShopStatus("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    when (response.body()!!.approvalStatus) {
                        "INCOMPLETE" -> {
                            navController.navigate("shop_registration/step3") {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                        "PENDING" -> {
                            navController.navigate("pending_approval") {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                        "APPROVED" -> {
                            navController.navigate("shop/home") {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                        "REJECTED" -> {
                            navController.navigate("rejected_screen") {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                        else -> {
                            navController.navigate(Screen.RoleSelection.route) {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                } else {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            } catch (e: Exception) {
                navController.navigate(Screen.RoleSelection.route) {
                    popUpTo(Screen.Splash.route) {
                        inclusive = true
                    }
                }
            }
        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }
        }
    }

    // Animation values
    val logoScale = remember { Animatable(0.3f) }
    val titleAlpha = remember { Animatable(0f) }
    val captionAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // A. Logo scale-in: springs from 0.3f to 1f
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        // B. Title + subtitle fade in: tween(600ms) with 300ms delay
        launch {
            delay(300)
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        // C. Bottom caption: fadeIn tween(400ms) with 800ms delay
        launch {
            delay(800)
            captionAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    RepaiiroTheme(useDarkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDark),
            contentAlignment = Alignment.Center
        ) {
            // Background image with low opacity
            Image(
                painter = painterResource(id = R.drawable.splash_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.18f // Low opacity
            )

            // Main content column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha.value }
            ) {
                // Subtle orange glow behind logo circle
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Glow box
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(OrangePrimary.copy(alpha = 0.25f), Color.Transparent),
                                    radius = 200f
                                ),
                                shape = CircleShape
                            )
                    )

                    // Logo image with round shape, border and shadow
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Repairo Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .graphicsLayer {
                                scaleX = logoScale.value
                                scaleY = logoScale.value
                            }
                            .shadow(8.dp, CircleShape)
                            .border(2.dp, OrangePrimary, CircleShape)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title + Subtitle Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Repairo",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CAR REPAIR APP",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangePrimary,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Bottom Caption
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Manage. Monitor. Optimize.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.graphicsLayer { alpha = captionAlpha.value }
                )
            }
        }
    }
}