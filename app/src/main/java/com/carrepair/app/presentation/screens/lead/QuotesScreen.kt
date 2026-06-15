package com.carrepair.app.presentation.screens.lead

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.data.dto.quote.QuoteResponseDto
import com.carrepair.app.domain.viewmodels.quotes.AcceptQuoteUiState
import com.carrepair.app.domain.viewmodels.quotes.QuotesViewModel
import com.carrepair.app.presentation.ui.theme.*
import com.carrepair.app.presentation.components.DarkNavHeader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    leadId: Long,
    leadStatus: String,
    navController: NavController,
    viewModel: QuotesViewModel
) {
    val quotes by viewModel.quotes.collectAsState()
    val acceptUiState by viewModel.acceptUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(leadId) { viewModel.loadQuotes(leadId) }

    LaunchedEffect(acceptUiState) {
        when (val state = acceptUiState) {
            is AcceptQuoteUiState.Success -> {
                snackbarHostState.showSnackbar("Quote accepted!")
                viewModel.resetAcceptState()
                val channelId = state.channelId
                if (channelId != null) {
                    navController.navigate("chat/$channelId") { popUpTo("leads/my") { inclusive = false } }
                } else {
                    navController.navigate("leads/my") { popUpTo(0) { inclusive = true } }
                }
            }
            is AcceptQuoteUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetAcceptState()
            }
            else -> {}
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                DarkNavHeader(
                    title = "Received Quotes",
                    onBack = { navController.popBackStack() }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (quotes.isEmpty()) {
                    PulsingEmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(quotes) { quote ->
                            StyledQuoteCard(
                                quote = quote,
                                leadStatus = leadStatus,
                                leadId = leadId,               // ← ADD
                                navController = navController, // ← ADD
                                onAccept = { quoteId -> viewModel.acceptQuote(leadId, quoteId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StyledQuoteCard(
    quote: QuoteResponseDto,
    leadStatus: String,
    leadId: Long,              // ← ADD
    navController: NavController,  // ← ADD
    onAccept: (Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Accept this quote?") },
            text = { Text("${quote.shopName} for PKR ${"%.0f".format(quote.price)}") },
            confirmButton = { TextButton(onClick = { showDialog = false; onAccept(quote.id) }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorder),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = quote.shopLogoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(1.dp, LightBorder, CircleShape)
                        .clickable {
                            navController.navigate(
                                com.carrepair.app.presentation.screens.Screen.ShopProfile.createRoute(
                                    shopId = quote.repairShopId,
                                    shopName = quote.shopName,
                                    logoUrl = quote.shopLogoUrl
                                )
                            )
                        }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote.shopName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark,
                        modifier = Modifier.clickable {
                            navController.navigate(
                                com.carrepair.app.presentation.screens.Screen.ShopProfile.createRoute(
                                    shopId = quote.repairShopId,
                                    shopName = quote.shopName,
                                    logoUrl = quote.shopLogoUrl
                                )
                            )
                        }
                    )
                    
                    val avgRating = quote.averageRating ?: 0.0
                    val count = quote.reviewCount ?: 0
                    if (count > 0 && avgRating > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "%,.1f".format(avgRating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "($count reviews)",
                                fontSize = 12.sp,
                                color = TextSubtle
                            )
                        }
                    } else {
                        Text(
                            text = "No reviews yet",
                            fontSize = 12.sp,
                            color = TextSubtle,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Text(text = formatTimeAgo(quote.createdAt), fontSize = 12.sp, color = TextSubtle, modifier = Modifier.padding(top = 2.dp))
                }
                Text(
                    text = "PKR %,.0f".format(quote.price),
                    fontSize = 18.sp,
                    color = OrangePrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (!quote.message.isNullOrBlank()) {
                Surface(color = LightBackground, shape = MaterialTheme.shapes.small) {
                    Text(
                        text = quote.message,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = TextDark
                    )
                }
            }

            if (leadStatus == "OPEN" && quote.status == "PENDING") {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Accept Quote", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            if (quote.status == "ACCEPTED") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = StatusGreenTint,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                            Text("Quote Accepted", style = MaterialTheme.typography.labelMedium, color = StatusGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = {
                            val encodedShop = java.net.URLEncoder.encode(quote.shopName, "UTF-8")
                            navController.navigate(
                                "payment/confirm/$leadId/${quote.id}/${quote.price}/$encodedShop"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pay Now — PKR %,.0f".format(quote.price), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }


        }
    }
}

@Composable
private fun PulsingEmptyState(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse),
        label = "alpha"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp).alpha(alpha))
        Spacer(Modifier.height(12.dp))
        Text(text = "Waiting for quotes...", modifier = Modifier.alpha(alpha), color = TextSubtle)
    }
}

private fun formatTimeAgo(createdAt: String): String {
    return try {
        val dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now())
        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else -> "${minutes / 1440}d ago"
        }
    } catch (e: Exception) { createdAt }
}
