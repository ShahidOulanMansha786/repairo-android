package com.carrepair.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.dto.lead.LeadResponseDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.components.LeadStatusChip
import com.carrepair.app.presentation.components.ShimmerCard
import com.carrepair.app.presentation.ui.theme.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    tokenManager: TokenManager,
    authApi: AuthApi,
    navController: NavController,
    onLogout: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var leads by remember { mutableStateOf<List<LeadResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val token = tokenManager.getAccessToken()
                    RetrofitClient.authApi.updateFcmToken(
                        token = "Bearer $token",
                        body = AuthApi.FcmTokenRequestDto(fcmToken = fcmToken)
                    )
                } catch (ignored: Exception) {}
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val token = tokenManager.getAccessToken()
            val authResponse = authApi.getMe("Bearer $token")
            if (authResponse.isSuccessful) {
                userName = authResponse.body()?.fullName ?: ""
            }
            val leadsResponse = RetrofitClient.leadApi.getMyLeads("Bearer $token")
            if (leadsResponse.isSuccessful) {
                leads = leadsResponse.body() ?: emptyList()
            }
        } catch (ignored: Exception) {
        } finally {
            isLoading = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val badgeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val activeCount = leads.count { it.status.uppercase() in listOf("PENDING", "OPEN") }
    val progressCount = leads.count { it.status.uppercase() in listOf("IN_PROGRESS", "IN PROGRESS") }
    val completedCount = leads.count { it.status.uppercase() == "COMPLETED" }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Welcome back,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = userName.ifBlank { "Car Owner" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { /* Notified used in original, keeping it */ },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = { navController.navigate("post_lead/step1") },
                            color = OrangePrimary,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline, 
                                    contentDescription = null, 
                                    tint = Color.White, 
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Create New Lead", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                modifier = Modifier.padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Repair Leads (${leads.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { navController.navigate("leads/my") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (isLoading) {
                        items(3) {
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { ShimmerCard() }
                        }
                    } else if (leads.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                Text("No repair leads found. Create one above!", color = TextSubtle)
                            }
                        }
                    } else {
                        itemsIndexed(leads) { _, lead ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                HomeLeadCard(
                                    lead = lead,
                                    badgeAlpha = badgeAlpha,
                                    onClick = { navController.navigate("leads/${lead.id}") },
                                    onViewQuotes = { navController.navigate("leads/${lead.id}/quotes?leadStatus=OPEN") }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(36.dp))
                        Text(
                            text = "STATUS SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSubtle,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatusSummaryCard(count = activeCount, label = "ACTIVE", modifier = Modifier.weight(1f))
                            StatusSummaryCard(count = progressCount, label = "IN PROGRESS", modifier = Modifier.weight(1f))
                            StatusSummaryCard(count = completedCount, label = "COMPLETED", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeLeadCard(
    lead: LeadResponseDto,
    badgeAlpha: Float,
    onClick: () -> Unit,
    onViewQuotes: () -> Unit
) {
    val formattedDate = remember(lead.createdAt) {
        try {
            val dateTime = LocalDateTime.parse(lead.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        } catch (e: Exception) {
            lead.createdAt.substringBefore("T")
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorder.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    text = "${lead.carMake} ${lead.carModel} ${lead.carYear}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = lead.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeadStatusChip(status = lead.title)
                val isPending = lead.status.uppercase() in listOf("PENDING", "OPEN")
                Box(Modifier.graphicsLayer { if (isPending) alpha = badgeAlpha }) {
                    LeadStatusChip(status = lead.status)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                val isPending = lead.status.uppercase() in listOf("PENDING", "OPEN")
                val isCompleted = lead.status.uppercase() == "COMPLETED"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { if (isPending) onViewQuotes() else onClick() }
                ) {
                    Text(
                        text = if (isPending) "View Quotes" else if (isCompleted) "Leave Review" else "Message Shop",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.StarOutline else if (isPending) Icons.Default.ChevronRight else Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusSummaryCard(count: Int, label: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LightBorder.copy(alpha = 0.4f)),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall, color = TextDark, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = TextSubtle, 
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}
