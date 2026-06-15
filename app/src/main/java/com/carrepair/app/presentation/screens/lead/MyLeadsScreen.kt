package com.carrepair.app.presentation.screens.lead

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.lead.LeadResponseDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.components.LeadStatusChip
import com.carrepair.app.presentation.components.ShimmerCard
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLeadsScreen(
    navController: NavController,
    leadApi: LeadApi,
    tokenManager: TokenManager
) {
    val role = remember { tokenManager.getRole() ?: "CAR_OWNER" }
    val isShop = role == "SHOP_OWNER"

    // Screen Entrance Animation Trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    if (isShop) {
        // SHOP MY QUOTES SCREEN
        MyQuotesShopView(navController)
    } else {
        // CAR OWNER MY LEADS SCREEN
        MyLeadsOwnerView(navController, leadApi, tokenManager, visible)
    }
}

@Composable
private fun MyQuotesShopView(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground
        ) { paddingValues ->
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                modifier = Modifier.padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                ) {
                    DarkNavHeader(
                        title = "My Quotes",
                        subtitle = "Track submitted quotes"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = NavInactive,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Quotes Yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSubtle
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your submitted quotes will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyLeadsOwnerView(
    navController: NavController,
    leadApi: LeadApi,
    tokenManager: TokenManager,
    screenVisible: Boolean
) {
    var leads by remember { mutableStateOf<List<LeadResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val token = "Bearer " + tokenManager.getAccessToken()
            val response = leadApi.getMyLeads(token)
            if (response.isSuccessful) {
                leads = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load leads"
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            topBar = {
                DarkNavHeader(
                    title = "My Leads",
                    onBack = { navController.popBackStack() }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("post_lead/step1") },
                    containerColor = OrangePrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Post a Lead")
                }
            }
        ) { padding ->
            AnimatedVisibility(
                visible = screenVisible,
                enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                modifier = Modifier.padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                ) {
                    when {
                        isLoading -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(4) {
                                    ShimmerCard()
                                }
                            }
                        }

                        errorMessage != null -> {
                            Text(
                                text = errorMessage ?: "Unknown error",
                                modifier = Modifier.align(Alignment.Center),
                                color = StatusRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        leads.isEmpty() -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "No leads posted yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = { navController.navigate("post_lead/step1") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("Post a Lead", color = Color.White)
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                itemsIndexed(leads) { index, lead ->
                                    var cardVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(lead.id) {
                                        delay(index * 70L)
                                        cardVisible = true
                                    }

                                    AnimatedVisibility(
                                        visible = cardVisible,
                                        enter = slideInVertically(animationSpec = tween(300)) { it / 4 } + fadeIn(animationSpec = tween(300))
                                    ) {
                                        OwnerLeadCard(
                                            lead = lead,
                                            onClick = { navController.navigate("leads/${lead.id}") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerLeadCard(
    lead: LeadResponseDto,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lead.carMake} ${lead.carModel}".uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lead.createdAt.substringBefore("T"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lead.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeadStatusChip(status = lead.title)
                LeadStatusChip(status = lead.status)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun MyLeadsScreenPreview() {
    // Preview is supported!
}