package com.carrepair.app.presentation.screens.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.domain.viewmodels.QuoteSubmitUiState
import com.carrepair.app.domain.viewmodels.ShopHomeViewModel
import com.carrepair.app.domain.viewmodels.ShopLeadDetailViewModel
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitQuoteScreen(
    leadId: Long,
    navController: NavController,
    shopHomeViewModel: ShopHomeViewModel,
    shopLeadDetailViewModel: ShopLeadDetailViewModel
) {
    val homeUiState by shopHomeViewModel.uiState.collectAsState()
    val quoteUiState by shopLeadDetailViewModel.quoteUiState.collectAsState()

    val lead = remember(homeUiState) {
        (homeUiState as? com.carrepair.app.domain.viewmodels.ShopHomeUiState.Success)
            ?.leads
            ?.find { it.id == leadId }
    }

    var price by remember { mutableStateOf("") }
    var completionTime by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var visible by remember { mutableStateOf(false) }
    var showQuoteSuccess by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(quoteUiState) {
        if (quoteUiState is QuoteSubmitUiState.Success) {
            shopHomeViewModel.loadBrowseData()
            showQuoteSuccess = true
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Quote?", style = MaterialTheme.typography.titleMedium, color = TextDark) },
            text = {
                Text(
                    "Are you sure you want to cancel? Your quote details will be lost.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtle
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Yes, Cancel", color = StatusRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Editing", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    RepaiiroTheme(useDarkTheme = false) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = LightBackground
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Bar
                    DarkNavHeader(
                        title = "Submit Quote",
                        onBack = { navController.popBackStack() }
                    )

                    // Hero Image Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (lead.imageUrls.isNotEmpty()) {
                            AsyncImage(
                                model = lead.imageUrls.first(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(NavyMedium),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                            }
                        }

                        // Bottom overlays
                        Surface(
                            color = OrangePrimary,
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "PENDING LEAD",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = lead.title.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Info Card
                    Surface(
                        color = LightSurface,
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, LightBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Location", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                Text(
                                    text = lead.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                            Divider(color = LightBorder, thickness = 1.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                                Text(
                                    lead.title.replace("_", " "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Form Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Price Estimate
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Price Estimate", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                leadingIcon = { Text("$ ", color = TextDark, fontWeight = FontWeight.Bold) },
                                placeholder = { Text("350", color = TextSubtle) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = MaterialTheme.shapes.small,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = LightBorder,
                                    focusedContainerColor = LightSurface,
                                    unfocusedContainerColor = LightSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Set a competitive price for this service.", style = MaterialTheme.typography.bodySmall, color = TextSubtle)
                        }

                        // Completion Time
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Completion Time", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = completionTime,
                                onValueChange = { completionTime = it },
                                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = NavInactive) },
                                placeholder = { Text("e.g., 2-3 days", color = TextSubtle) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.small,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = LightBorder,
                                    focusedContainerColor = LightSurface,
                                    unfocusedContainerColor = LightSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Message (Optional)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Message (Optional)", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                placeholder = { Text("Add any additional details or offers...", color = TextSubtle) },
                                minLines = 4,
                                maxLines = 6,
                                shape = MaterialTheme.shapes.small,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = LightBorder,
                                    focusedContainerColor = LightSurface,
                                    unfocusedContainerColor = LightSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (quoteUiState is QuoteSubmitUiState.Error) {
                            Text(
                                text = (quoteUiState as QuoteSubmitUiState.Error).message,
                                color = StatusRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Submit Button
                        PrimaryButton(
                            text = "✓ Submit Quote",
                            enabled = price.isNotBlank(),
                            isLoading = quoteUiState is QuoteSubmitUiState.Loading,
                            onClick = {
                                val finalMessage = buildString {
                                    if (completionTime.isNotBlank()) {
                                        append("Completion: $completionTime\n")
                                    }
                                    if (message.isNotBlank()) {
                                        append(message)
                                    }
                                }
                                shopLeadDetailViewModel.submitQuote(
                                    leadId = leadId,
                                    price = price,
                                    message = finalMessage.ifBlank { null }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Cancel Button
                        TextButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Cancel", color = TextSubtle, style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(8.dp).navigationBarsPadding())

                        // Pro Tip Card
                        Surface(
                            color = StatusBlueTint,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = StatusBlue, modifier = Modifier.size(24.dp))
                                Column {
                                    Text("Pro Tip", style = MaterialTheme.typography.titleSmall, color = TextDark, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Submit a competitive quote to increase your chances. Once accepted, you'll receive a notification and can start messaging the customer.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            AnimatedVisibility(
                visible = showQuoteSuccess,
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
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Quote Submitted!",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your quote has been sent. You'll be notified when the customer responds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    showQuoteSuccess = false
                                    shopLeadDetailViewModel.resetState()
                                    navController.popBackStack()
                                    navController.popBackStack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text(
                                    "Back to Browse",
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
