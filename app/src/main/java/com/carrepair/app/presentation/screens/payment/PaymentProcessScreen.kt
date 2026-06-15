package com.carrepair.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.data.apis.PaymentApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.components.PulsingRing
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val processingSteps = listOf(
    "Encrypting payment data...",
    "Securing your funds in escrow...",
    "Verifying transaction details...",
    "Connecting to payment gateway...",
    "Almost there..."
)

@Composable
fun PaymentProcessScreen(
    navController: NavController,
    paymentId: Long,
    leadId: Long,
    paymentUrl: String,       // kept for signature compatibility, no longer used
    amount: Double,           // ← NEW: pass amount so card form can show it
    shopName: String,         // ← NEW: pass shop name for card form header
    paymentApi: PaymentApi,
    tokenManager: TokenManager
) {
    var currentStep by remember { mutableStateOf(0) }
    var navigated by remember { mutableStateOf(false) }

    // Animate through steps, then navigate to card form
    LaunchedEffect(Unit) {
        for (i in processingSteps.indices) {
            currentStep = i
            delay(550)
        }
        if (!navigated) {
            navigated = true
            val encodedShop = URLEncoder.encode(shopName, StandardCharsets.UTF_8.toString())
            navController.navigate(
                "payment/checkout/$paymentId/$leadId/$amount/$encodedShop"
            ) {
                // Don't pop process screen — let back stack handle it naturally
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "process")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rotation"
    )
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "iconScale"
    )

    RepaiiroTheme(useDarkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(NavyDark, Color(0xFF0D1B35), NavyDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                // Animated lock with pulsing ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    PulsingRing(color = OrangePrimary, size = 140.dp)

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .graphicsLayer { rotationZ = rotation }
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier
                                .size(36.dp)
                                .scale(iconScale)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Preparing Checkout",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Setting up your secure payment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                // Animated step text
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(tween(300)) + slideInVertically(tween(300)) { 20 } togetherWith
                                fadeOut(tween(200))
                    },
                    label = "step"
                ) { step ->
                    Surface(
                        color = Color(0xFF1A2A42),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = OrangePrimary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = processingSteps.getOrElse(step) { processingSteps.last() },
                                style = MaterialTheme.typography.labelMedium,
                                color = TextOffWhite
                            )
                        }
                    }
                }

                // Progress dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(processingSteps.size) { i ->
                        AnimatedVisibility(visible = i <= currentStep) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == currentStep) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == currentStep) OrangePrimary
                                        else OrangePrimary.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

object PaymentDeepLinkState {
    var pendingLeadId: Long = 0L
}
