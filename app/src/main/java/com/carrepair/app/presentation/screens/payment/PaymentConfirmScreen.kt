package com.carrepair.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PaymentConfirmScreen(
    navController: NavController,
    leadId: Long,
    quoteId: Long,
    quotedPrice: Double,
    shopName: String,
    paymentApi: PaymentApi,
    tokenManager: TokenManager
) {
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(paymentApi, tokenManager)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animated entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val fee = quotedPrice * 3.0 / 100.0
    val shopReceives = quotedPrice - fee

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is PaymentUiState.SessionCreated -> {
                val encodedUrl = URLEncoder.encode(s.session.paymentUrl, StandardCharsets.UTF_8.toString())
                val encodedShop = URLEncoder.encode(shopName, StandardCharsets.UTF_8.toString())
                navController.navigate(
                    "payment/process/${s.session.paymentId}/$leadId" +
                            "?url=$encodedUrl" +
                            "&amount=${s.session.amount}" +
                            "&shop=$encodedShop"
                )
                viewModel.resetState()
            }
            is PaymentUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DarkNavHeader(
                    title = "Confirm Payment",
                    subtitle = "Review before proceeding",
                    onBack = { navController.popBackStack() }
                )
            },
            containerColor = LightBackground
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ScreenPadding, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Shop Identity Card ────────────────────────────────
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { -30 }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = LightSurface,
                            border = BorderStroke(1.dp, LightBorder),
                            tonalElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(OrangeSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shopName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Verified Repair Shop",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSubtle
                                    )
                                }
                                Surface(
                                    color = StatusGreenTint,
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, null, tint = StatusGreen, modifier = Modifier.size(11.dp))
                                        Text("Verified", style = MaterialTheme.typography.labelSmall, color = StatusGreen)
                                    }
                                }
                            }
                        }
                    }

                    // ── Fee Breakdown Card ────────────────────────────────
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { 40 }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = LightSurface,
                            border = BorderStroke(1.dp, LightBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                // Section label
                                Text(
                                    text = "PAYMENT BREAKDOWN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSubtle,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(16.dp))

                                FeeRow(
                                    label = "Repair Quote",
                                    value = "PKR %,.0f".format(quotedPrice),
                                    valueColor = TextDark
                                )
                                Spacer(Modifier.height(10.dp))
                                FeeRow(
                                    label = "Platform Fee (3%)",
                                    value = "- PKR %,.0f".format(fee),
                                    valueColor = TextSubtle
                                )
                                Spacer(Modifier.height(14.dp))
                                Divider(color = LightBorder)
                                Spacer(Modifier.height(14.dp))

                                // Total row — highlighted
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "You Pay",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "PKR %,.0f".format(quotedPrice),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(14.dp))
                                Divider(color = LightBorder)
                                Spacer(Modifier.height(14.dp))

                                FeeRow(
                                    label = "Shop Receives",
                                    value = "PKR %,.0f".format(shopReceives),
                                    labelColor = TextSubtle,
                                    valueColor = StatusGreen
                                )
                            }
                        }
                    }

                    // ── Escrow Info Banner ────────────────────────────────
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(600)) { 40 }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = OrangeSubtle,
                            border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(OrangePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text(
                                            "Protected by Escrow",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = OrangePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Funds released only after job completion",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OrangePrimary.copy(0.7f)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Divider(color = OrangePrimary.copy(alpha = 0.15f))
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    EscrowBullet("Your money is safe until the repair is done", modifier = Modifier.weight(1f))
                                    EscrowBullet("48-hour dispute window after completion", modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // ── Security Badges ───────────────────────────────────
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600))
                    ) {
                        SecureBadgeRow()
                    }

                    Spacer(Modifier.height(80.dp)) // space for floating button
                }

                // ── Floating Pay Button ───────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(500)) { 100 } + fadeIn(tween(500)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Surface(
                        color = LightSurface,
                        shadowElevation = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            PrimaryButton(
                                text = if (uiState is PaymentUiState.Loading) ""
                                       else "Pay PKR %,.0f Securely".format(quotedPrice),
                                onClick = { viewModel.initiatePayment(leadId, quoteId) },
                                modifier = Modifier.fillMaxWidth(),
                                isLoading = uiState is PaymentUiState.Loading
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tap to proceed to secure checkout",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EscrowBullet(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = OrangePrimary, modifier = Modifier.size(14.dp).padding(top = 1.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = OrangePrimary.copy(0.8f), lineHeight = 16.sp)
    }
}
