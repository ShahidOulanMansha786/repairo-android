package com.carrepair.app.presentation.screens.lead

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.jobtracking.JobProgressDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.viewmodels.ChatViewModel
import com.carrepair.app.domain.viewmodels.LeadDetailViewModel
import com.carrepair.app.domain.viewmodels.LeadDetailViewModelFactory
import com.carrepair.app.domain.viewmodels.PaymentUiState
import com.carrepair.app.domain.viewmodels.PaymentViewModel
import com.carrepair.app.domain.viewmodels.PaymentViewModelFactory
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.components.LeadStatusChip
import com.carrepair.app.presentation.screens.payment.EscrowStatusCard
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.LightBorder
import com.carrepair.app.presentation.ui.theme.LightSurface
import com.carrepair.app.presentation.ui.theme.NavyDark
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.StatusRed
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextMuted
import com.carrepair.app.presentation.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LeadDetailScreen(
    navController: NavController,
    leadId: Long,
    leadApi: LeadApi,
    tokenManager: TokenManager,
    chatViewModel: ChatViewModel
) {

    val viewModel: LeadDetailViewModel = viewModel(
        factory = LeadDetailViewModelFactory(leadApi, tokenManager)
    )
    val lead = viewModel.lead
    val isLoading = viewModel.isLoading
    val jobSteps = viewModel.jobSteps

    var showCancelDialog by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }
    var chatRequested by remember { mutableStateOf(false) }
    // ── NEW state variables ──────────────────────────────────────────────────
    var showSatisfiedDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    var showReviewButton by remember { mutableStateOf(false) }
    var shopNameForReview by remember { mutableStateOf("") }
    var shopIdForReview by remember { mutableStateOf(0L) }
    // ────────────────────────────────────────────────────────────────────────

    val snackbarHostState = remember { SnackbarHostState() }
    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(RetrofitClient.paymentApi, tokenManager)
    )
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    val paymentStatus by paymentViewModel.paymentStatus.collectAsState()

    val chatChannelId by chatViewModel.channelId.collectAsState()
    val chatLoading by chatViewModel.isLoading.collectAsState()
    val chatError by chatViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLead(leadId)
    }

    LaunchedEffect(lead?.status) {
        val s = lead?.status?.uppercase()
        if (s == "CLOSED" || s == "IN_PROGRESS" || s == "COMPLETED") {
            paymentViewModel.loadPaymentStatus(leadId)
            viewModel.loadJobProgress(leadId)
        }
        if (s == "COMPLETED") {
            try {
                val token = "Bearer " + tokenManager.getAccessToken()
                val quotesRes = leadApi.getQuotesForLead(token, leadId)
                if (quotesRes.isSuccessful) {
                    val acceptedQuote = quotesRes.body()?.find { it.status.uppercase() == "ACCEPTED" }
                    if (acceptedQuote != null) {
                        shopIdForReview = acceptedQuote.repairShopId
                        shopNameForReview = acceptedQuote.shopName
                        val reviewsRes = RetrofitClient.reviewApi.getReviewsForShop(token, acceptedQuote.repairShopId)
                        if (reviewsRes.isSuccessful) {
                            val exists = reviewsRes.body()?.any { it.leadId == leadId } == true
                            showReviewButton = !exists
                        } else {
                            showReviewButton = true
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

//    LaunchedEffect(leadId) {
//        try {
//            val token = "Bearer " + tokenManager.getAccessToken()
//            val response = leadApi.getJobProgress(leadId)
//            if (response.isSuccessful) {
//                jobSteps = response.body() ?: emptyList()
//            }
//        } catch (e: Exception) { }
//    }
    // ────────────────────────────────────────────────────────────────────────

//    LaunchedEffect(lead?.status) {
//        val s = lead?.status?.uppercase()
//        if (s == "CLOSED" || s == "IN_PROGRESS" || s == "COMPLETED") {
//            paymentViewModel.loadPaymentStatus(leadId)
//        }
//    }

    LaunchedEffect(paymentUiState) {
        if (paymentUiState is PaymentUiState.Released) {
            paymentViewModel.loadPaymentStatus(leadId)
            viewModel.refreshAll(leadId)
        }
    }

    LaunchedEffect(chatChannelId) {
        if (chatRequested && chatChannelId != null) {
            chatRequested = false
            navController.navigate("chat/${chatChannelId}")
        }
    }

    LaunchedEffect(chatError) {
        if (chatRequested && chatError != null) {
            chatRequested = false
            snackbarHostState.showSnackbar(chatError ?: "Failed to open chat")
            chatViewModel.reset()
        }
    }

    // ── Cancel dialog (original) ─────────────────────────────────────────────
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Lead") },
            text = { Text("Are you sure you want to cancel this lead?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        isCancelling = true
                    }
                ) { Text("Yes, Cancel", color = StatusRed) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }

    // ── NEW: Satisfied dialog ────────────────────────────────────────────────
    if (showSatisfiedDialog) {
        AlertDialog(
            onDismissRequest = { showSatisfiedDialog = false },
            title = { Text("Confirm Job Complete") },
            text = { Text("Are you satisfied with the repair? This will release payment to the shop.") },
            confirmButton = {
                TextButton(onClick = {
                    showSatisfiedDialog = false
                    paymentStatus?.let { paymentViewModel.releaseImmediately(it.paymentId) }
                    viewModel.refreshAll(leadId)
                }) { Text("Yes, I'm Satisfied", color = Color.Green) }
            },
            dismissButton = {
                TextButton(onClick = { showSatisfiedDialog = false }) { Text("No") }
            }
        )
    }

    // ── NEW: Dispute dialog ──────────────────────────────────────────────────
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text("Raise a Dispute") },
            text = { Text("Are you sure you want to raise a dispute? This will pause the payment release.") },
            confirmButton = {
                TextButton(onClick = {
                    showDisputeDialog = false
                    navController.navigate("dispute/raise/${lead?.id}")
                }) { Text("Yes, Raise Dispute", color = StatusRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) { Text("Cancel") }
            }
        )
    }
    // ────────────────────────────────────────────────────────────────────────

    LaunchedEffect(isCancelling) {
        if (isCancelling) {
            viewModel.cancelLead(
                leadId,
                onSuccess = { isCancelling = false },
                onError = { isCancelling = false }
            )
        }
    }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            topBar = {
                DarkNavHeader(
                    title = "Lead Details",
                    onBack = { navController.popBackStack() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(LightBackground)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = OrangePrimary
                        )
                    }

                    lead != null -> {
                        val currentLead = lead!!
                        val pagerState = rememberPagerState(
                            pageCount = { if (currentLead.imageUrls.isEmpty()) 1 else currentLead.imageUrls.size }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // IMAGE PAGER SECTION
                            Box {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                ) { page ->
                                    if (currentLead.imageUrls.isEmpty()) {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            color = LightBackground
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(80.dp),
                                                    tint = TextMuted
                                                )
                                            }
                                        }
                                    } else {
                                        AsyncImage(
                                            model = currentLead.imageUrls[page],
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                if (currentLead.imageUrls.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 12.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        repeat(currentLead.imageUrls.size) { index ->
                                            val isSelected = pagerState.currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isSelected) 8.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // STATUS & ID
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LeadStatusChip(status = currentLead.status)
                                    Text(
                                        text = "ID: #${currentLead.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSubtle
                                    )
                                }

                                // CAR DETAILS CARD
                                DetailSection(title = "Vehicle Information") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        InfoItem(label = "Make", value = currentLead.carMake, modifier = Modifier.weight(1f))
                                        InfoItem(label = "Model", value = currentLead.carModel, modifier = Modifier.weight(1f))
                                        InfoItem(label = "Year", value = currentLead.carYear.toString(), modifier = Modifier.weight(1f))
                                    }
                                }

                                // DESCRIPTION
                                DetailSection(title = "Issue Description") {
                                    Text(
                                        text = currentLead.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDark,
                                        lineHeight = 22.sp
                                    )
                                }

                                // LOCATION
                                DetailSection(title = "Repair Location") {
                                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                                        Text(
                                            text = currentLead.address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextDark
                                        )
                                    }
                                }

                                val statusUpper = currentLead.status.uppercase()
                                if (statusUpper == "CLOSED" || statusUpper == "IN_PROGRESS" || statusUpper == "COMPLETED") {
                                    if (jobSteps.isNotEmpty()) {
                                        DetailSection(title = "Job Progress") {
                                            JobProgressTimeline(steps = jobSteps)
                                        }
                                    }
                                }

                                // ESCROW STATUS CARD
                                if (statusUpper == "CLOSED" || statusUpper == "IN_PROGRESS" || statusUpper == "COMPLETED") {
                                    paymentStatus?.let { ps ->
                                        EscrowStatusCard(
                                            paymentStatus = ps,
                                            isLoading = paymentUiState is PaymentUiState.Loading,
                                            onConfirmJobComplete = { paymentId ->
                                                paymentViewModel.releaseImmediately(paymentId)
                                            },
                                            onViewFullStatus = {
                                                navController.navigate("payment/escrow/${currentLead.id}")
                                            }
                                        )
                                    }
                                }

                                // ACTION BUTTONS
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val btnStatusUpper = currentLead.status.uppercase()

                                    if (btnStatusUpper == "OPEN" || btnStatusUpper == "PENDING" || btnStatusUpper == "CLOSED") {
                                        Button(
                                            onClick = {
                                                navController.navigate("leads/${currentLead.id}/quotes?leadStatus=${currentLead.status}")
                                            },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                        ) {
                                            Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("View Quotes", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (btnStatusUpper == "CLOSED" || btnStatusUpper == "IN_PROGRESS" || btnStatusUpper == "IN PROGRESS") {
                                        Button(
                                            onClick = {
                                                chatRequested = true
                                                chatViewModel.loadChannel(leadId)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                            enabled = !chatLoading
                                        ) {
                                            if (chatLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                            } else {
                                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Message Shop", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (btnStatusUpper == "COMPLETED" && showReviewButton) {
                                         Button(
                                             onClick = {
                                                 val carInfo = "${currentLead.carMake} ${currentLead.carModel}"
                                                 navController.navigate(
                                                     com.carrepair.app.presentation.screens.Screen.LeaveReview.createRoute(
                                                         leadId = currentLead.id,
                                                         shopName = shopNameForReview,
                                                         shopId = shopIdForReview,
                                                         carInfo = carInfo
                                                     )
                                                 )
                                             },
                                             modifier = Modifier.fillMaxWidth().height(52.dp),
                                             shape = RoundedCornerShape(12.dp),
                                             colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                         ) {
                                             Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                                             Spacer(Modifier.width(8.dp))
                                             Text("Leave a Review", fontWeight = FontWeight.Bold)
                                         }
                                     }

                                     // ── NEW: Satisfied / Dispute buttons ────────────────────────────
                                    val shopMarkedDone = jobSteps.any { it.step == "SHOP_MARKED_DONE" && it.completed }
                                    val alreadyDisputed = jobSteps.any { it.step == "DISPUTE_RAISED" }
                                    val alreadySatisfied = jobSteps.any { it.step == "CAR_OWNER_SATISFIED" && it.completed }

                                    if (shopMarkedDone && !alreadySatisfied && !alreadyDisputed &&
                                        (btnStatusUpper == "CLOSED" || btnStatusUpper == "IN_PROGRESS")
                                    ) {
                                        Button(
                                            onClick = { showSatisfiedDialog = true },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                        ) {
                                            Text("Yes, I am Satisfied", fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { showDisputeDialog = true },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, StatusRed)
                                        ) {
                                            Text("Raise a Dispute", color = StatusRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    // ────────────────────────────────────────────────────────────────

                                    // Cancel Lead Button
                                    if (btnStatusUpper == "OPEN" || btnStatusUpper == "PENDING") {
                                        Button(
                                            onClick = { showCancelDialog = true },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                                        ) {
                                            if (isCancelling) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                            } else {
                                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Cancel This Repair Lead", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSubtle,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LightSurface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSubtle)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

// ── NEW: Job Progress Timeline composable ───────────────────────────────────
@Composable
private fun JobProgressTimeline(steps: List<JobProgressDto>) {
    val orderedSteps = listOf(
        "QUOTE_ACCEPTED", "PAYMENT_RECEIVED", "WORK_IN_PROGRESS",
        "SHOP_MARKED_DONE", "DISPUTE_RAISED", "DISPUTE_RESOLVED",
        "CAR_OWNER_SATISFIED", "COMPLETED"
    )

    val hasDispute = steps.any { it.step == "DISPUTE_RAISED" }

    val displaySteps = if (hasDispute) {
        orderedSteps.filter { it != "CAR_OWNER_SATISFIED" && it != "COMPLETED" }
    } else {
        orderedSteps.filter { it != "DISPUTE_RAISED" && it != "DISPUTE_RESOLVED" }
    }

    val stepLabels = mapOf(
        "QUOTE_ACCEPTED"     to "Quote Accepted",
        "PAYMENT_RECEIVED"   to "Payment Secured",
        "WORK_IN_PROGRESS"   to "Work In Progress",
        "SHOP_MARKED_DONE"   to "Repair Shop Marked Done",
        "CAR_OWNER_SATISFIED" to "Car Owner Satisfied",
        "COMPLETED"          to "Completed",
        "DISPUTE_RAISED"     to "Dispute Raised",
        "DISPUTE_RESOLVED"   to "Dispute Resolved"
    )

    val completedSteps = steps.filter { it.completed }.map { it.step }
    val lastCompletedIndex = displaySteps.indexOfLast { it in completedSteps }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        displaySteps.forEachIndexed { index, stepKey ->
            val stepData = steps.find { it.step == stepKey }
            val isCompleted = stepData?.completed == true
            val isActive = !isCompleted && index == lastCompletedIndex + 1

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> Color(0xFF2E7D32)
                                isActive    -> OrangePrimary
                                else        -> Color.LightGray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Circle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column {
                    Text(
                        text = stepLabels[stepKey] ?: stepKey,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCompleted -> TextDark
                            isActive    -> OrangePrimary
                            else        -> TextMuted
                        }
                    )
                    if (stepData?.completedAt != null) {
                        Text(
                            text = stepData.completedAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSubtle
                        )
                    }
                }
            }
        }
    }
}
// ────────────────────────────────────────────────────────────────────────────