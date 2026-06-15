package com.carrepair.app.presentation.screens.lead

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LeadSuccessScreen(
    navController: NavController,
    leadId: Long
) {
    // Intercept back presses
    BackHandler { }

    // Checkmark scale animation
    var scaleTarget by remember { mutableStateOf(0f) }
    val checkmarkScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CheckmarkScale"
    )

    LaunchedEffect(Unit) {
        scaleTarget = 1f
    }

    // Bottom sheet slide-up animation
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        cardVisible = true
    }

    // Steps stagger animations
    var step1Visible by remember { mutableStateOf(false) }
    var step2Visible by remember { mutableStateOf(false) }
    var step3Visible by remember { mutableStateOf(false) }
    LaunchedEffect(cardVisible) {
        if (cardVisible) {
            delay(100)
            step1Visible = true
            delay(100)
            step2Visible = true
            delay(100)
            step3Visible = true
        }
    }

    RepaiiroTheme(useDarkTheme = true) {
        Scaffold(
            containerColor = NavyDark
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NavyDark)
            ) {
                // TOP SECTION (~40% of screen)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Checkmark Circle with spring-scale
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .graphicsLayer {
                                scaleX = checkmarkScale
                                scaleY = checkmarkScale
                            }
                            .clip(CircleShape)
                            .background(OrangePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Lead Submitted!",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 26.sp),
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your repair request is now live",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                // BOTTOM CARD (White slide-up sheet, takes ~60% of screen)
                AnimatedVisibility(
                    visible = cardVisible,
                    enter = slideInVertically(
                        animationSpec = tween(500)
                    ) { it } + fadeIn(animationSpec = tween(500)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                ) {
                    Surface(
                        color = LightSurface,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = "What happens next?",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Step 1
                            AnimatedVisibility(
                                visible = step1Visible,
                                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                            ) {
                                StepRow(
                                    number = "1",
                                    title = "Repair shops are being notified",
                                    description = "Your lead is now visible to qualified repair shops in your area."
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Step 2
                            AnimatedVisibility(
                                visible = step2Visible,
                                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                            ) {
                                StepRow(
                                    number = "2",
                                    title = "Shops will review your request",
                                    description = "They'll assess the details and photos you provided."
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Step 3
                            AnimatedVisibility(
                                visible = step3Visible,
                                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                            ) {
                                StepRow(
                                    number = "3",
                                    title = "You'll receive competitive quotes",
                                    description = "Compare pricing and services to choose best option."
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tip Card
                            Surface(
                                color = OrangeSubtle,
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Tip",
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Tip: Most customers receive their first quote within 2-4 hours.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Button
                            PrimaryButton(
                                text = "Got it, Thanks!",
                                onClick = {
                                    navController.navigate("leads/my") {
                                        popUpTo("leads/success/$leadId") { inclusive = true }
                                    }
                                },
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
private fun StepRow(
    number: String,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Circle Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(OrangePrimary)
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSubtle
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun LeadSuccessScreenPreview() {
    // Preview is supported!
}