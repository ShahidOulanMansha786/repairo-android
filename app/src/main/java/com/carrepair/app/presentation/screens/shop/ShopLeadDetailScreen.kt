package com.carrepair.app.presentation.screens.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.domain.viewmodels.ChatViewModel
import com.carrepair.app.domain.viewmodels.QuoteSubmitUiState
import com.carrepair.app.domain.viewmodels.ShopHomeViewModel
import com.carrepair.app.domain.viewmodels.ShopLeadDetailViewModel
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.components.StatusChip
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ShopLeadDetailScreen(
    leadId: Long,
    navController: NavController,
    shopHomeViewModel: ShopHomeViewModel,
    shopLeadDetailViewModel: ShopLeadDetailViewModel,
    chatViewModel: ChatViewModel,
    onSubmitQuote: () -> Unit = {}
) {
    val homeUiState by shopHomeViewModel.uiState.collectAsState()
    val creditsBalance by shopHomeViewModel.creditsBalance.collectAsState()
    val quoteUiState by shopLeadDetailViewModel.quoteUiState.collectAsState()
    val chatChannelId by chatViewModel.channelId.collectAsState()
    val chatLoading by chatViewModel.isLoading.collectAsState()
    val chatError by chatViewModel.error.collectAsState()

    var chatRequested by remember { mutableStateOf(false) }

    val lead = remember(homeUiState) {
        (homeUiState as? com.carrepair.app.domain.viewmodels.ShopHomeUiState.Success)
            ?.leads
            ?.find { it.id == leadId }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isLockedInitially = remember(lead) {
        lead?.let { item ->
            when {
                item.locked == true -> true
                item.unlocked == true -> false
                else -> item.id % 2 == 0L
            }
        } ?: (leadId % 2 == 0L)
    }
    var isUnlockedLocal by remember(leadId, isLockedInitially) { mutableStateOf(!isLockedInitially) }
    var isUnlockingLoading by remember { mutableStateOf(false) }
    var showUnlockSuccess by remember { mutableStateOf(false) }
    var showInsufficientCredits by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        shopHomeViewModel.loadShopCredits()
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

    LaunchedEffect(quoteUiState) {
        if (quoteUiState is QuoteSubmitUiState.Success) {
            snackbarHostState.showSnackbar("Quote submitted!")
            shopHomeViewModel.loadBrowseData()
            shopLeadDetailViewModel.resetState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(showUnlockSuccess) {
        if (showUnlockSuccess) {
            delay(3000)
            showUnlockSuccess = false
        }
    }

    if (showInsufficientCredits) {
        AlertDialog(
            onDismissRequest = { showInsufficientCredits = false },
            title = { Text("Not Enough Credits", style = MaterialTheme.typography.titleMedium, color = TextDark) },
            text = {
                Text(
                    "You need at least 1 credit to unlock this lead.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtle
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInsufficientCredits = false
                        navController.navigate("shop/buy_credits")
                    }
                ) {
                    Text("Buy Credits", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsufficientCredits = false }) {
                    Text("Cancel", color = TextSubtle)
                }
            }
        )
    }

    RepaiiroTheme(useDarkTheme = false) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = LightBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (lead != null) {
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
                            if (!isUnlockedLocal) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PrimaryButton(
                                        text = "🔒 Unlock Lead (1 Credit)",
                                        isLoading = isUnlockingLoading,
                                        onClick = {
                                            val balance = creditsBalance
                                            if (balance != null && balance <= 0) {
                                                showInsufficientCredits = true
                                            } else if (!shopHomeViewModel.consumeCredit()) {
                                                showInsufficientCredits = true
                                            } else {
                                                scope.launch {
                                                    isUnlockingLoading = true
                                                    delay(800)
                                                    isUnlockedLocal = true
                                                    isUnlockingLoading = false
                                                    showUnlockSuccess = true
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "By unlocking, you agree to our Marketplace Terms",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSubtle,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                PrimaryButton(
                                    text = "➤ Submit Quote",
                                    onClick = onSubmitQuote,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (lead == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Lead not found", color = TextDark)
                }
                return@Scaffold
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                modifier = Modifier.padding(paddingValues)
            ) {
                // Crossfade between locked and unlocked state representation
                AnimatedContent(
                    targetState = isUnlockedLocal,
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                    },
                    label = "LockUnlockState"
                ) { unlocked ->
                    if (!unlocked) {
                        // ── LOCKED STATE VIEW ──
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LightBackground)
                                .verticalScroll(rememberScrollState())
                        ) {
                            DarkNavHeader(
                                title = "Unlock Lead",
                                onBack = { navController.popBackStack() },
                                trailingContent = {
                                    creditsBalance?.let { balance ->
                                        Text(
                                            text = "$balance Credit${if (balance == 1) "" else "s"}",
                                            color = OrangePrimary,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            )

                            // Blurred Photo Preview Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                if (lead.imageUrls.isNotEmpty()) {
                                    AsyncImage(
                                        model = lead.imageUrls.first(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(16.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(NavyMedium)
                                    )
                                }
                                // Dimming overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )

                                // Centered preview info
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(NavyMedium.copy(alpha = 0.8f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = TextWhite,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = "Limited Preview",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Unlock this lead to view full high-resolution photos and customer contact information.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Text(
                                    text = "📷 ${lead.imageUrls.size} PHOTOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                )
                            }

                            // Content Details
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Info Grid (Category + Vehicle Type)
                                Surface(
                                    color = LightSurface,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, LightBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(lead.title.uppercase(), style = MaterialTheme.typography.bodyMedium, color = TextDark, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Vehicle Type", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${lead.carMake} ${lead.carModel}", style = MaterialTheme.typography.bodyMedium, color = TextDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Location & Time row
                                Surface(
                                    color = LightSurface,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, LightBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                                            Text(
                                                text = lead.address,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "2 hours ago",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSubtle
                                        )
                                    }
                                }

                                // Issue summary
                                Surface(
                                    color = StatusAmberTint,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, StatusAmber.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "ISSUE SUMMARY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            lead.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextDark
                                        )
                                    }
                                }

                                // Service & Urgency Grid
                                Surface(
                                    color = LightSurface,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, LightBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Service Needed", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(lead.title.replace("_", " ").uppercase(), style = MaterialTheme.typography.bodyMedium, color = TextDark, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Urgency", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Flexible", style = MaterialTheme.typography.bodyMedium, color = OrangePrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // How it Works
                                Surface(
                                    color = StatusBlueTint,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, StatusBlue.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = StatusBlue, modifier = Modifier.size(22.dp))
                                        Column {
                                            Text("How it works", style = MaterialTheme.typography.titleSmall, color = TextDark, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Unlocking a lead gives you instant access to the customer's phone number and exact address. Credits are only deducted if the lead is valid.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextDark
                                            )
                                        }
                                    }
                                }

                                // Unlock Cost Row
                                Surface(
                                    color = LightSurface,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, LightBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(OrangeSubtle)
                                            ) {
                                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                                            }
                                            Column {
                                                Text("Unlock Cost", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                                Text("1 Credit", style = MaterialTheme.typography.titleSmall, color = TextDark, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("BALANCE AFTER", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                            Text(
                                                "${((creditsBalance ?: 1) - 1).coerceAtLeast(0)} Credits",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    } else {
                        // ── UNLOCKED STATE VIEW ──
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LightBackground)
                                .verticalScroll(rememberScrollState())
                        ) {
                            DarkNavHeader(
                                title = "Lead Details",
                                subtitle = "🔓 Unlocked • Full access",
                                onBack = { navController.popBackStack() }
                            )

                            // Image Horizontal Slider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                if (lead.imageUrls.isNotEmpty()) {
                                    val pagerState = rememberPagerState(pageCount = { lead.imageUrls.size })
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        AsyncImage(
                                            model = lead.imageUrls[page],
                                            contentDescription = "Lead image $page",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Dots indicator
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 12.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        repeat(lead.imageUrls.size) { index ->
                                            Box(
                                                modifier = Modifier
                                                    .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f)
                                                    )
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No images", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Chips overlays
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatusChip(label = lead.title)
                                    StatusChip(label = "UNLOCKED")
                                }
                            }

                            // Main Details
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${lead.carMake} ${lead.carModel} (${lead.carYear})",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lead.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSubtle,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Location & Date row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp))
                                        Text(lead.address, style = MaterialTheme.typography.bodySmall, color = TextSubtle)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp))
                                        Text(
                                            lead.createdAt.substringBefore("T"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSubtle
                                        )
                                    }
                                }

                                // Damage Photos lazyrow list
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Damage Photos", style = MaterialTheme.typography.titleSmall, color = TextDark, fontWeight = FontWeight.Bold)
                                        Text("${lead.imageUrls.size} photos", style = MaterialTheme.typography.labelSmall, color = OrangePrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "Review these photos to assess the damage before submitting your quote",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSubtle
                                    )

                                    if (lead.imageUrls.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            itemsIndexed(lead.imageUrls) { i, url ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 100.dp, height = 80.dp)
                                                        .clip(MaterialTheme.shapes.large)
                                                ) {
                                                    AsyncImage(
                                                        model = url,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Surface(
                                                        color = Color.Black.copy(alpha = 0.6f),
                                                        shape = MaterialTheme.shapes.extraSmall,
                                                        modifier = Modifier
                                                            .padding(4.dp)
                                                            .align(Alignment.TopStart)
                                                    ) {
                                                        Text(
                                                            text = (i + 1).toString(),
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Customer Contact Card
                                Surface(
                                    color = LightSurface,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(1.dp, LightBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("Customer Contact", style = MaterialTheme.typography.titleSmall, color = TextDark, fontWeight = FontWeight.Bold)

                                        // Name Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = NavInactive, modifier = Modifier.size(20.dp))
                                                Text("Name", style = MaterialTheme.typography.bodyMedium, color = TextSubtle)
                                            }
                                            Text("Not provided", style = MaterialTheme.typography.bodyMedium, color = TextSubtle, fontWeight = FontWeight.Medium)
                                        }

                                        Divider(color = LightBorder, thickness = 1.dp)

                                        // Phone Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = NavInactive, modifier = Modifier.size(20.dp))
                                                Text("Phone", style = MaterialTheme.typography.bodyMedium, color = TextSubtle)
                                            }
                                            Text("Not provided", style = MaterialTheme.typography.bodyMedium, color = TextSubtle, fontWeight = FontWeight.Medium)
                                        }

                                        Divider(color = LightBorder, thickness = 1.dp)

                                        // Email Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Email, contentDescription = null, tint = NavInactive, modifier = Modifier.size(20.dp))
                                                Text("Email", style = MaterialTheme.typography.bodyMedium, color = TextSubtle)
                                            }
                                            Text("Not provided", style = MaterialTheme.typography.bodyMedium, color = TextSubtle, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showUnlockSuccess,
                enter = scaleIn(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    initialScale = 0.85f
                ) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OverlayScrim),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = LightSurface,
                        modifier = Modifier
                            .padding(32.dp)
                            .width(280.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            var checkVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { checkVisible = true }
                            val checkScale by animateFloatAsState(
                                targetValue = if (checkVisible) 1f else 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "CheckScale"
                            )
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(checkScale)
                                    .clip(CircleShape)
                                    .background(StatusGreenTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = StatusGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Lead Unlocked!",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "You now have full access to customer contact details.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showUnlockSuccess = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text(
                                    "View Full Details",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
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