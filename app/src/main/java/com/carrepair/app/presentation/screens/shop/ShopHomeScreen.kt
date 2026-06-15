package com.carrepair.app.presentation.screens.shop

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.dto.lead.NearbyLeadResponseDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.viewmodels.ShopHomeUiState
import com.carrepair.app.domain.viewmodels.ShopHomeViewModel
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.components.ShimmerCard
import com.carrepair.app.presentation.components.StatusChip
import com.carrepair.app.presentation.ui.theme.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopHomeScreen(
    viewModel: ShopHomeViewModel,
    tokenManager: TokenManager,
    onLeadClick: (Long) -> Unit,
    onBuyCreditsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val creditsBalance by viewModel.creditsBalance.collectAsState()

    var searchCategory by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var visible by remember { mutableStateOf(false) }

    val categoryFilters = remember {
        listOf("All", "Body Damage", "Mechanical", "Electrical", "Paint", "Interior")
    }

    // Screen Entrance Animation Trigger
    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(Unit) {
        viewModel.loadBrowseData()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadBrowseData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // FCM token registration logic (preserved)
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val token = tokenManager.getAccessToken()
                        RetrofitClient.authApi.updateFcmToken(
                            token = "Bearer $token",
                            body = AuthApi.FcmTokenRequestDto(fcmToken = fcmToken)
                        )
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to update token", e)
                    }
                }
            }
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            bottomBar = {
                // Fixed bottom Buy Credits Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightSurface)
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(color = LightBorder, thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        PrimaryButton(
                            text = "Buy Lead Credits",
                            onClick = onBuyCreditsClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
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
                    // 1. Dark NAV HEADER
                    DarkNavHeader(
                        title = "Browse Leads",
                        subtitle = "and repair opportunities",
                        trailingContent = {
                            Surface(
                                color = NavyMedium,
                                shape = MaterialTheme.shapes.extraSmall,
                                border = BorderStroke(1.dp, NavyBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = "Credits",
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = creditsBalance?.let { "$it Credits" } ?: "Credits",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    )

                    // 2. SEARCH & FILTER ROW (Light styling)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBackground)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchCategory,
                            onValueChange = { searchCategory = it },
                            placeholder = { Text("Search by category...", color = TextSubtle) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = NavInactive
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                focusedPlaceholderColor = TextSubtle,
                                unfocusedPlaceholderColor = TextSubtle,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = LightBorder,
                                focusedContainerColor = LightSurface,
                                unfocusedContainerColor = LightSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            onClick = {
                                searchCategory = ""
                                selectedCategory = "All"
                            },
                            color = LightSurface,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, LightBorder),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Clear filters",
                                    tint = NavInactive
                                )
                            }
                        }
                    }

                    // Category filter chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBackground)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryFilters) { category ->
                            val isSelected = selectedCategory == category
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) OrangePrimary else LightSurface,
                                animationSpec = tween(300),
                                label = "ChipBg"
                            )
                            val chipText by animateColorAsState(
                                targetValue = if (isSelected) Color.White else TextSubtle,
                                animationSpec = tween(300),
                                label = "ChipText"
                            )
                            Surface(
                                onClick = { selectedCategory = category },
                                color = chipBg,
                                shape = MaterialTheme.shapes.extraSmall,
                                border = if (isSelected) null else BorderStroke(1.dp, LightBorder)
                            ) {
                                Text(
                                    text = category,
                                    color = chipText,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 3. MAIN CONTENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (val state = uiState) {
                            is ShopHomeUiState.Loading -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(4) {
                                        ShimmerCard()
                                    }
                                }
                            }

                            is ShopHomeUiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = state.message,
                                        color = StatusRed,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Button(
                                        onClick = { viewModel.loadNearbyLeads() },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                    ) {
                                        Text("Retry", color = Color.White)
                                    }
                                }
                            }

                            is ShopHomeUiState.Success -> {
                                val filteredLeads = remember(state.leads, searchCategory, selectedCategory) {
                                    state.leads.filter { lead ->
                                        val matchesSearch = searchCategory.isBlank() ||
                                            lead.title.contains(searchCategory, ignoreCase = true) ||
                                            lead.carMake.contains(searchCategory, ignoreCase = true) ||
                                            lead.carModel.contains(searchCategory, ignoreCase = true) ||
                                            lead.description.contains(searchCategory, ignoreCase = true)

                                        val categoryKey = selectedCategory.replace(" ", "_")
                                        val matchesCategory = selectedCategory == "All" ||
                                            lead.title.contains(selectedCategory, ignoreCase = true) ||
                                            lead.title.contains(categoryKey, ignoreCase = true)

                                        matchesSearch && matchesCategory
                                    }
                                }

                                if (filteredLeads.isEmpty()) {
                                    val serverEmpty = state.leads.isEmpty()
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (serverEmpty) {
                                                Icons.Default.Inbox
                                            } else {
                                                Icons.Default.SearchOff
                                            },
                                            contentDescription = null,
                                            tint = NavInactive,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = if (serverEmpty) {
                                                "No leads available"
                                            } else {
                                                "No leads found"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextSubtle
                                        )
                                        Text(
                                            text = if (serverEmpty) {
                                                "The server returned no nearby leads. Ensure leads are OPEN, your shop is approved, and your shop location is set."
                                            } else {
                                                "Try a different category or search term."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSubtle,
                                            textAlign = TextAlign.Center
                                        )
                                        if (serverEmpty) {
                                            Button(
                                                onClick = { viewModel.loadBrowseData() },
                                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                            ) {
                                                Text("Refresh", color = Color.White)
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        itemsIndexed(filteredLeads) { index, lead ->
                                            var cardVisible by remember { mutableStateOf(false) }
                                            LaunchedEffect(Unit) {
                                                delay(index * 70L)
                                                cardVisible = true
                                            }

                                            val cardAlpha by animateFloatAsState(
                                                targetValue = if (cardVisible) 1f else 0f,
                                                animationSpec = tween(300),
                                                label = "CardAlpha"
                                            )
                                            val cardOffsetY by animateFloatAsState(
                                                targetValue = if (cardVisible) 0f else 30f,
                                                animationSpec = tween(300),
                                                label = "CardOffsetY"
                                            )
                                            Box(
                                                modifier = Modifier.graphicsLayer {
                                                    alpha = cardAlpha
                                                    translationY = cardOffsetY
                                                }
                                            ) {
                                                NearbyShopLeadCard(
                                                    lead = lead,
                                                    onLeadClick = onLeadClick
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NearbyShopLeadCard(
    lead: NearbyLeadResponseDto,
    onLeadClick: (Long) -> Unit
) {
    val isLocked = when {
        lead.locked == true -> true
        lead.unlocked == true -> false
        else -> lead.id % 2 == 0L
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = LightSurface,
        border = BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ROW 1: Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                StatusChip(label = lead.title)

                // Locked badge chip if applicable
                if (isLocked) {
                    Surface(
                        color = StatusAmberTint,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = "3 CREDITS TO UNLOCK",
                            color = StatusAmber,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ROW 2: Icon + Title details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left lock/eye badge 44dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isLocked) NavyMedium else StatusGreenTint)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Key else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (isLocked) NavInactive else StatusGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Right details column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${lead.carMake} ${lead.carModel} (${lead.carYear})",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isLocked) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lead.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ROW 3: Details (Distance + Time ago)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(14.dp)
                    )
                    val distanceMeters = lead.distanceMeters ?: 0.0
                    val distanceText = if (distanceMeters < 1000) {
                        "${distanceMeters.toInt()}m away"
                    } else {
                        "${"%.1f".format(distanceMeters / 1000)}km away"
                    }
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }

                // Time ago
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = lead.createdAt.substringBefore("T"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ROW 4: Status + photo count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lead.status.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${lead.imageUrls.size} photos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSubtle
                )
            }

            // Divider
            Divider(
                color = LightBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // ROW 5: Action — wired to onLeadClick
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLeadClick(lead.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLocked) "Unlock Lead" else "View Full Details",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ShopHomeScreenPreview() {
    // Preview is supported!
}