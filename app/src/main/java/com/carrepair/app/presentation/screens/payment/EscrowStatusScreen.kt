package com.carrepair.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carrepair.app.data.apis.PaymentApi
import com.carrepair.app.data.dto.payment.PaymentStatusResponseDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.viewmodels.PaymentUiState
import com.carrepair.app.domain.viewmodels.PaymentViewModel
import com.carrepair.app.domain.viewmodels.PaymentViewModelFactory
import com.carrepair.app.presentation.components.*
import com.carrepair.app.presentation.ui.theme.*

@Composable
fun EscrowStatusScreen(
    navController: NavController,
    leadId: Long,
    paymentApi: PaymentApi,
    tokenManager: TokenManager
) {
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(paymentApi, tokenManager)
    )
    val uiState by viewModel.uiState.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showReleaseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadPaymentStatus(leadId) }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is PaymentUiState.Released -> {
                snackbarHostState.showSnackbar("Payment released to shop successfully!")
                viewModel.loadPaymentStatus(leadId)
            }
            is PaymentUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    // Release confirmation dialog
    if (showReleaseDialog) {
        AlertDialog(
            onDismissRequest = { showReleaseDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = StatusGreen) },
            title = {
                Text(
                    "Confirm Job Completion",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This will release PKR %,.0f to the repair shop. Only confirm if the repair has been completed to your satisfaction.".format(
                        paymentStatus?.amountPayableToShop ?: 0.0
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReleaseDialog = false
                        paymentStatus?.paymentId?.let { viewModel.releaseImmediately(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Yes, Release Funds", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseDialog = false }) {
                    Text("Not Yet", color = TextSubtle)
                }
            }
        )
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DarkNavHeader(
                    title = "Escrow Status",
                    subtitle = "Track your payment",
                    onBack = { navController.popBackStack() }
                )
            },
            containerColor = LightBackground
        ) { padding ->
            when {
                uiState is PaymentUiState.Loading && paymentStatus == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = OrangePrimary)
                            Text("Loading payment status...", color = TextSubtle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                paymentStatus != null -> {
                    val status = paymentStatus!!
                    EscrowStatusContent(
                        status = status,
                        modifier = Modifier.padding(padding),
                        isLoading = uiState is PaymentUiState.Loading,
                        onRelease = { showReleaseDialog = true },
                        onRefresh = { viewModel.loadPaymentStatus(leadId) }
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No payment found for this lead", color = TextSubtle)
                    }
                }
            }
        }
    }
}

@Composable
private fun EscrowStatusContent(
    status: PaymentStatusResponseDto,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onRelease: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenPadding, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Main Escrow Card ──────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = LightSurface,
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ESCROW BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    EscrowStatusChip(status = status.escrowStatus)
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        status.currency,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtle,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        "%,.0f".format(status.amountTotal),
                        style = MaterialTheme.typography.displaySmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = LightBorder)

                // Fee details
                FeeRow("Platform Fee", "PKR %,.0f".format(status.platformFee), valueColor = TextSubtle)
                FeeRow("Shop Receives", "PKR %,.0f".format(status.amountPayableToShop), valueColor = StatusGreen)
            }
        }

        // ── Countdown timer (if FUNDS_RECEIVED and eligibleReleaseAt set) ─
        if (status.escrowStatus == "FUNDS_RECEIVED" && status.eligibleReleaseAt != null) {
            val countdown = computeCountdown(status.eligibleReleaseAt)
            if (countdown != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = StatusBlueTint,
                    border = BorderStroke(1.dp, StatusBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StatusBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Auto-Release", style = MaterialTheme.typography.labelMedium, color = StatusBlue, fontWeight = FontWeight.Bold)
                            Text(countdown, style = MaterialTheme.typography.bodySmall, color = StatusBlue.copy(0.7f))
                        }
                    }
                }
            }
        }

        // ── Escrow Timeline ────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = LightSurface,
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "PAYMENT TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(16.dp))
                EscrowTimeline(escrowStatus = status.escrowStatus)
            }
        }

        // ── Dispute window note ───────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = StatusAmberTint,
            border = BorderStroke(1.dp, StatusAmber.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                Text(
                    "${status.disputeWindowHours}-hour dispute window applies after job completion",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusAmber,
                    lineHeight = 18.sp
                )
            }
        }

        // ── Released state ────────────────────────────────────────────────
        if (status.escrowStatus == "RELEASED_TO_SHOP") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = StatusGreenTint,
                border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(32.dp))
                    Text(
                        "Payment Released",
                        style = MaterialTheme.typography.titleSmall,
                        color = StatusGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "PKR %,.0f has been sent to the shop".format(status.amountPayableToShop),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── DISPUTED state ────────────────────────────────────────────────
        if (status.escrowStatus == "DISPUTED") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = StatusAmberTint,
                border = BorderStroke(1.dp, StatusAmber.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = StatusAmber, modifier = Modifier.size(32.dp))
                    Text(
                        "Dispute Under Review",
                        style = MaterialTheme.typography.titleSmall,
                        color = StatusAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Our team will review within 24 hours. Payment is on hold.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusAmber.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── RETURNED state ────────────────────────────────────────────────
        if (status.escrowStatus == "RETURNED") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = StatusRedTint,
                border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AssignmentReturn, null, tint = StatusRed, modifier = Modifier.size(32.dp))
                    Text(
                        "Funds Returned",
                        style = MaterialTheme.typography.titleSmall,
                        color = StatusRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "PKR %,.0f has been returned to you after dispute resolution.".format(status.amountTotal),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusRed.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── EscrowTimeline — dispute path bhi handle karta hai ───────────────────────
@Composable
private fun EscrowTimeline(escrowStatus: String) {
    val steps = if (escrowStatus == "DISPUTED" || escrowStatus == "RETURNED") {
        listOf(
            "Payment Initiated" to "Transaction started",
            "Funds Secured"     to "Held safely in escrow",
            "Repair Complete"   to "Shop finished work",
            "Dispute Raised"    to "Under admin review",
            if (escrowStatus == "RETURNED")
                "Funds Returned"  to "Refunded to owner"
            else
                "Dispute Resolved" to "Pending resolution"
        )
    } else {
        listOf(
            "Payment Initiated" to "Transaction started",
            "Funds Secured"     to "Held safely in escrow",
            "Repair Complete"   to "Shop finished work",
            "Funds Released"    to "Shop payment sent"
        )
    }

    val activeIndex = when (escrowStatus) {
        "INITIATED"        -> 0
        "FUNDS_RECEIVED"   -> 1
        "RELEASED_TO_SHOP" -> 3
        "DISPUTED"         -> 3
        "RETURNED"         -> 4
        else               -> 0
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        steps.forEachIndexed { index, (title, subtitle) ->
            val isCompleted = index < activeIndex
            val isActive    = index == activeIndex

            val isDisputeStep = (escrowStatus == "DISPUTED" || escrowStatus == "RETURNED") && index == 3
            val isReturnedStep = escrowStatus == "RETURNED" && index == 4

            val dotBorderColor = when {
                isDisputeStep && (isActive || isCompleted) -> StatusRed
                isActive || isCompleted                     -> OrangePrimary
                else                                        -> LightBorder
            }
            val dotBgColor = when {
                isDisputeStep && isCompleted  -> StatusRed
                isReturnedStep && isCompleted -> StatusRed
                isCompleted                   -> OrangePrimary
                else                          -> Color.Transparent
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, dotBorderColor, CircleShape)
                        .background(dotBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        val icon = when (index) {
                            0    -> Icons.Default.PlayArrow
                            1    -> Icons.Default.Lock
                            2    -> Icons.Default.Build
                            3    -> if (escrowStatus == "DISPUTED" || escrowStatus == "RETURNED")
                                Icons.Default.Warning
                            else
                                Icons.Default.CheckCircle
                            else -> Icons.Default.CheckCircle
                        }
                        val iconTint = when {
                            isDisputeStep && isActive -> StatusRed
                            isActive                  -> OrangePrimary
                            else                      -> TextMuted
                        }
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
                    }
                }

                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isDisputeStep && (isActive || isCompleted) -> StatusRed
                            isReturnedStep && isCompleted              -> StatusRed
                            isActive                                    -> OrangePrimary
                            isCompleted                                 -> TextDark
                            else                                        -> TextMuted
                        }
                    )
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                }
            }
        }
    }
}
