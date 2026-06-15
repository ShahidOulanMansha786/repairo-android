package com.carrepair.app.presentation.screens.shop.quotes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.jobtracking.JobProgressDto
import com.carrepair.app.data.dto.quote.ShopQuoteResponse
import com.carrepair.app.domain.viewmodels.quotes.QuoteDetailViewModel
import com.carrepair.app.domain.viewmodels.quotes.QuoteDetailViewModelFactory
import kotlinx.coroutines.delay
import kotlin.collections.find
import kotlin.collections.forEachIndexed

val NavyDark = Color(0xFF0D1B2A)
val OrangeAccent = Color(0xFFE8622A)
//val GreenCheck = Color(0xFF2ECC71)
//val StepPending = Color(0xFFCCCCCC)
//val CardBg = Color(0xFFF8F8F8)
//val DisputeRed = Color(0xFFE53935)
//
//// ── Ordered steps for both paths ────────────────────────────────────────────
//private val ORDERED_STEPS = listOf(
//    "QUOTE_ACCEPTED",
//    "PAYMENT_RECEIVED",
//    "WORK_IN_PROGRESS",
//    "SHOP_MARKED_DONE",
//    "DISPUTE_RAISED",
//    "DISPUTE_RESOLVED",
//    "CAR_OWNER_SATISFIED",
//    "COMPLETED"
//)
//
//private val STEP_LABELS = mapOf(
//    "QUOTE_ACCEPTED"      to "Quote Accepted",
//    "PAYMENT_RECEIVED"    to "Payment Received",
//    "WORK_IN_PROGRESS"    to "Work In Progress",
//    "SHOP_MARKED_DONE"    to "Shop Owner Marked Done",
//    "CAR_OWNER_SATISFIED" to "Car Owner Satisfied",
//    "COMPLETED"           to "Completed",
//    "DISPUTE_RAISED"      to "Dispute Raised",
//    "DISPUTE_RESOLVED"    to "Dispute Resolved"
//)
//
///** Returns the steps to display based on whether a dispute exists */
//private fun resolveDisplaySteps(progress: List<JobProgressDto>): List<String> {
//    val hasDispute = progress.any { it.step == "DISPUTE_RAISED" }
//    return if (hasDispute) {
//        // Dispute path: hide normal happy-path endings
//        ORDERED_STEPS.filter { it != "CAR_OWNER_SATISFIED" && it != "COMPLETED" }
//    } else {
//        // Normal path: hide dispute steps
//        ORDERED_STEPS.filter { it != "DISPUTE_RAISED" && it != "DISPUTE_RESOLVED" }
//    }
//}
// ────────────────────────────────────────────────────────────────────────────

private val ORDERED_STEPS = listOf(
    "QUOTE_ACCEPTED", "PAYMENT_RECEIVED", "WORK_IN_PROGRESS",
    "SHOP_MARKED_DONE", "DISPUTE_RAISED", "DISPUTE_RESOLVED",
    "CAR_OWNER_SATISFIED", "COMPLETED"
)

private val STEP_LABELS = mapOf(
    "QUOTE_ACCEPTED"      to "Quote Accepted",
    "PAYMENT_RECEIVED"    to "Payment Received",
    "WORK_IN_PROGRESS"    to "Work In Progress",
    "SHOP_MARKED_DONE"    to "Shop Marked Done",
    "CAR_OWNER_SATISFIED" to "Car Owner Satisfied",
    "COMPLETED"           to "Completed",
    "DISPUTE_RAISED"      to "Dispute Raised",
    "DISPUTE_RESOLVED"    to "Dispute Resolved"
)

private fun resolveDisplaySteps(progress: List<JobProgressDto>): List<String> {
    val hasDispute = progress.any { it.step == "DISPUTE_RAISED" && it.completed }
    return if (hasDispute) {
        ORDERED_STEPS.filter { it != "CAR_OWNER_SATISFIED" && it != "COMPLETED" }
    } else {
        ORDERED_STEPS.filter { it != "DISPUTE_RAISED" && it != "DISPUTE_RESOLVED" }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quote: ShopQuoteResponse,
    onBack: () -> Unit,
    onViewLeadDetails: (Long) -> Unit,
    navController: NavController,
    leadApi: LeadApi
) {
    val viewModel: QuoteDetailViewModel = viewModel(
        factory = QuoteDetailViewModelFactory(leadApi,  quote.leadId)
    )

    var showConfirmDialog by remember { mutableStateOf(false) }
    val channelId by viewModel.channelId.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val markDoneSuccess by viewModel.markDoneSuccess.collectAsState()
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()
    var navigatedToChat by remember { mutableStateOf(false) }

    LaunchedEffect(channelId) {
        if (channelId != null && !navigatedToChat) {
            navigatedToChat = true
            navController.navigate("chat/$channelId")
            viewModel.resetChannelId()
        }
    }

    LaunchedEffect(markDoneSuccess) {
        if (markDoneSuccess) viewModel.fetchProgress()
    }

    val paymentStep = progress.find { it.step == "PAYMENT_RECEIVED" }
    val shopMarkedDone = progress.find { it.step == "SHOP_MARKED_DONE" }
    val alreadyDisputed = progress.any { it.step == "DISPUTE_RAISED" && it.completed }
    val showMarkDoneButton = quote.status.uppercase() == "ACCEPTED" &&
            paymentStep?.completed == true &&
            shopMarkedDone?.completed != true &&
            !alreadyDisputed

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F6FA),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Quote Details",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "View and manage your quote",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
                )
            },
            bottomBar = {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.fetchChatChannel() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            border = BorderStroke(1.5.dp, OrangeAccent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Message", color = OrangeAccent, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { onViewLeadDetails(quote.leadId) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Lead Details", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Quote Info Card ──
                QuoteInfoCard(quote = quote)

                // ── Job Progress Card ──
                JobProgressCard(
                    progress = progress,
                    alreadyDisputed = alreadyDisputed,
                    showMarkDoneButton = showMarkDoneButton,
                    isLoading = isLoading,
                    onMarkDone = { showConfirmDialog = true }
                )

                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Success Animation Overlay ──
        if (showSuccessAnimation) {
            MarkDoneSuccessOverlay(
                onFinished = { viewModel.dismissSuccessAnimation() }
            )
        }
    }

    if (showConfirmDialog) {
        MarkDoneConfirmDialog(
            onConfirm = {
                showConfirmDialog = false
                viewModel.markWorkDone()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

// ── Quote Info Card ──────────────────────────────────────────────────────────
@Composable
private fun QuoteInfoCard(quote: ShopQuoteResponse) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuoteStatusBadge(status = quote.status)
                Text(
                    text = formatDate(quote.createdAt),
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "${quote.carMake} ${quote.carModel} ${quote.carYear}",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A2E)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(14.dp)
                )
                Text(quote.carOwnerName, color = Color(0xFF9E9E9E), fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            QuoteInfoTile(
                label = "YOUR QUOTE",
                value = "PKR ${quote.price}",
                valueColor = OrangeAccent,
                modifier = Modifier.weight(1f)
            )


            Spacer(Modifier.height(14.dp))

            Surface(
                color = Color(0xFFEEF4FF),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.FormatQuote,
                        null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            "Quote Note",
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "\"${quote.message}\"",
                            color = Color(0xFF1565C0),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Job Progress Card ────────────────────────────────────────────────────────
@Composable
private fun JobProgressCard(
    progress: List<JobProgressDto>,
    alreadyDisputed: Boolean,
    showMarkDoneButton: Boolean,
    isLoading: Boolean,
    onMarkDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Job Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                if (alreadyDisputed) {
                    DisputeBadge()
                }
            }

            Spacer(Modifier.height(20.dp))

            if (progress.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent, strokeWidth = 2.dp)
                }
            } else {
                val displaySteps = resolveDisplaySteps(progress)
                displaySteps.forEachIndexed { index, stepKey ->
                    val stepData = progress.find { it.step == stepKey }
                    TimelineStep(
                        stepKey = stepKey,
                        stepData = stepData,
                        isLast = index == displaySteps.size - 1
                    )
                }
            }

            if (showMarkDoneButton) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onMarkDone,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mark Work as Done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Timeline Step ────────────────────────────────────────────────────────────
@Composable
fun TimelineStep(
    stepKey: String,
    stepData: JobProgressDto?,
    isLast: Boolean
) {
    val isCompleted = stepData?.completed == true

    val dotColor = when {
        stepKey == "DISPUTE_RAISED" && isCompleted   -> Color(0xFFD32F2F)
        stepKey == "DISPUTE_RESOLVED" && isCompleted -> Color(0xFF2E7D32)
        stepKey == "SHOP_MARKED_DONE" && isCompleted -> OrangeAccent
        isCompleted                                   -> Color(0xFF2E7D32)
        else                                          -> Color(0xFFE0E0E0)
    }

    val icon: ImageVector? = when {
        stepKey == "DISPUTE_RAISED"                  -> Icons.Default.Warning
        stepKey == "DISPUTE_RESOLVED" && isCompleted -> Icons.Default.Verified
        stepKey == "SHOP_MARKED_DONE"                -> Icons.Default.ThumbUp
        isCompleted                                   -> Icons.Default.Check
        else                                          -> null
    }

    val label = STEP_LABELS[stepKey] ?: stepKey

    val subtitle = when {
        isCompleted && stepKey == "DISPUTE_RESOLVED" && stepData?.metadata != null ->
            when {
                stepData.metadata.contains("RELEASED_TO_SHOP")  -> "✓ Payment released to shop"
                stepData.metadata.contains("RETURNED_TO_OWNER") -> "✓ Payment returned to owner"
                else -> stepData.metadata
            }
        isCompleted && stepKey == "PAYMENT_RECEIVED" && stepData?.metadata != null ->
            "TX: ${stepData.metadata}"
        isCompleted && stepData?.completedAt != null ->
            "Completed ${stepData.completedAt.take(10)}"
        stepKey == "DISPUTE_RAISED" && !isCompleted   -> "Pending admin review"
        stepKey == "DISPUTE_RESOLVED" && !isCompleted -> "Awaiting resolution"
        stepKey == "SHOP_MARKED_DONE" && !isCompleted -> "Waiting for customer"
        else -> "Upcoming"
    }

    val labelColor = when {
        stepKey == "DISPUTE_RAISED" && isCompleted   -> Color(0xFFD32F2F)
        stepKey == "DISPUTE_RESOLVED" && isCompleted -> Color(0xFF2E7D32)
        isCompleted                                   -> Color(0xFF1A1A2E)
        else                                          -> Color(0xFFBDBDBD)
    }

    val subtitleColor = when {
        stepKey == "DISPUTE_RAISED"                      -> Color(0xFFD32F2F).copy(alpha = 0.8f)
        stepKey == "SHOP_MARKED_DONE" && !isCompleted    -> OrangeAccent
        stepKey == "DISPUTE_RESOLVED" && isCompleted     -> Color(0xFF2E7D32).copy(alpha = 0.8f)
        else                                              -> Color(0xFF9E9E9E)
    }

    val lineColor = when {
        stepKey == "DISPUTE_RAISED" && isCompleted -> Color(0xFFD32F2F).copy(alpha = 0.3f)
        isCompleted                                 -> Color(0xFF2E7D32).copy(alpha = 0.3f)
        else                                        -> Color(0xFFE0E0E0)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .then(
                        if (!isCompleted && stepKey != "DISPUTE_RAISED")
                            Modifier.border(2.dp, Color(0xFFE0E0E0), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    icon != null -> Icon(
                        icon, null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    !isCompleted -> Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBDBDBD))
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(lineColor)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .padding(top = 5.dp)
                .padding(bottom = if (isLast) 0.dp else 0.dp)
        ) {
            Text(
                label,
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 15.sp,
                color = labelColor
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = subtitleColor)
            Spacer(Modifier.height(if (isLast) 0.dp else 20.dp))
        }
    }
}

// ── Dispute Badge ────────────────────────────────────────────────────────────
@Composable
private fun DisputeBadge() {
    Surface(
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning, null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(12.dp)
            )
            Text(
                "Dispute Active",
                fontSize = 11.sp,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Quote Status Badge ───────────────────────────────────────────────────────
@Composable
private fun QuoteStatusBadge(status: String) {
    val (bg, fg) = when (status.uppercase()) {
        "ACCEPTED" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "PENDING"  -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        "REJECTED" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else       -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

// ── Quote Info Tile ──────────────────────────────────────────────────────────
@Composable
private fun QuoteInfoTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 10.sp, color = Color(0xFF9E9E9E), fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        }
    }
}

// ── Mark Done Success Overlay ────────────────────────────────────────────────
@Composable
private fun MarkDoneSuccessOverlay(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha",
        finishedListener = { if (it == 0f) onFinished() }
    )

    LaunchedEffect(Unit) {
        delay(2200)
        visible = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f * alpha))
            .graphicsLayer(alpha = alpha),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👍", fontSize = 38.sp)
                }
                Text(
                    "Work Marked as Done!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    "Customer has been notified\nfor final approval.",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ── Mark Done Confirm Dialog ─────────────────────────────────────────────────
@Composable
fun MarkDoneConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "dialogScale"
        )
        Box(
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(OrangeAccent.copy(alpha = 0.2f), OrangeAccent.copy(alpha = 0.05f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔧", fontSize = 32.sp)
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        "Mark Work as Complete?",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "The customer will be notified to review\nand approve the completed work.",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text("Cancel", color = Color(0xFF757575), fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ── Helper ───────────────────────────────────────────────────────────────────
private fun formatDate(dateStr: String): String {
    return try {
        val input = java.time.LocalDate.parse(dateStr.take(10))
        val month = input.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        "$month ${input.dayOfMonth}, ${input.year}"
    } catch (e: Exception) { dateStr.take(10) }
}