package com.carrepair.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carrepair.app.data.apis.PaymentApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.viewmodels.PaymentUiState
import com.carrepair.app.domain.viewmodels.PaymentViewModel
import com.carrepair.app.domain.viewmodels.PaymentViewModelFactory
import com.carrepair.app.presentation.components.*
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PaymentSuccessScreen(
    navController: NavController,
    paymentId: Long,
    leadId: Long,
    paymentApi: PaymentApi,
    tokenManager: TokenManager
) {
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(paymentApi, tokenManager)
    )
    val uiState by viewModel.uiState.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()

    // Animation states
    var checkVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var confettiActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPaymentStatus(leadId)
        delay(200)
        confettiActive = true
        delay(300)
        checkVisible = true
        delay(500)
        contentVisible = true
    }

    // Checkmark spring animation
    val checkScale by animateFloatAsState(
        targetValue = if (checkVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "checkScale"
    )

    RepaiiroTheme(useDarkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
        ) {
            // Confetti layer
            if (confettiActive) {
                ConfettiOverlay()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))

                // ── Animated Checkmark ────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    // Glow background
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(StatusGreen.copy(alpha = 0.2f), Color.Transparent)
                                )
                            )
                    )
                    // Circle + check
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(checkScale)
                            .clip(CircleShape)
                            .background(StatusGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Success Text ──────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 20 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Payment Successful!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Your funds are safely held in escrow",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Amount Card ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 30 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = LightSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Amount in Escrow",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSubtle
                            )
                            Text(
                                "PKR %,.0f".format(paymentStatus?.amountTotal ?: 0.0),
                                style = MaterialTheme.typography.displaySmall,
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            EscrowStatusChip(status = paymentStatus?.escrowStatus ?: "FUNDS_RECEIVED")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Escrow Timeline ───────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 30 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = LightSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "ESCROW TIMELINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            EscrowTimeline(
                                escrowStatus = paymentStatus?.escrowStatus ?: "FUNDS_RECEIVED"
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── What Happens Next ─────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 30 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = OrangeSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "WHAT HAPPENS NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangePrimary,
                                letterSpacing = 1.sp
                            )
                            NextStep("The repair shop will begin work on your vehicle")
                            NextStep("After completion, you'll have 48 hours to confirm")
                            NextStep("Funds are released to the shop after your confirmation")
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── CTA Button ────────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 40 }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PrimaryButton(
                            text = "Track Repair Progress",
                            onClick = {
                                navController.navigate("leads/$leadId") {
                                    popUpTo("home") { inclusive = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                        ) {
                            Text("Go to Home", color = TextDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun NextStep(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = OrangePrimary,
            modifier = Modifier.size(12.dp).padding(top = 2.dp)
        )
        Text(text, style = MaterialTheme.typography.bodySmall, color = OrangePrimary.copy(0.8f), lineHeight = 18.sp)
    }
}

// Simple confetti using Canvas
@Composable
private fun ConfettiOverlay() {
    val confettiColors = listOf(OrangePrimary, StatusGreen, StatusBlue, StatusAmber, Color(0xFFEC4899))
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                speed = 0.003f + Random.nextFloat() * 0.004f,
                size = 6f + Random.nextFloat() * 8f,
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                particles.forEach { p ->
                    val currentY = ((p.y + time * p.speed * 3f) % 1.2f)
                    val currentX = p.x + sin(time * 2f * Math.PI.toFloat() + p.rotation) * 0.02f
                    if (currentY < 0.6f) {
                        drawCircle(
                            color = p.color.copy(alpha = 1f - currentY * 1.5f),
                            radius = p.size,
                            center = Offset(currentX * size.width, currentY * size.height)
                        )
                    }
                }
            }
    )
}

private data class ConfettiParticle(
    val x: Float, val y: Float,
    val color: Color, val speed: Float,
    val size: Float, val rotation: Float
)
