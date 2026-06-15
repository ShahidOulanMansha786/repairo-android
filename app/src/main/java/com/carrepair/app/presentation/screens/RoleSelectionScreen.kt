package com.carrepair.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.LightBorder
import com.carrepair.app.presentation.ui.theme.LightSurface
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.OrangeSubtle
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.StatusBlue
import com.carrepair.app.presentation.ui.theme.StatusBlueTint
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextSubtle

@Composable
fun RoleSelectionScreen(navController: NavController) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    RepaiiroTheme(useDarkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(horizontal = 16.dp), // ScreenPadding
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top center: "Repairo"
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Repairo",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Main Heading
                    Text(
                        text = "Welcome to Repairo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = "Please select your account type to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtle,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Car Owner card
                    RoleCardRedesign(
                        title = "I am a Car Owner",
                        subtitle = "Manage your vehicle and service history",
                        icon = Icons.Default.DirectionsCar,
                        iconTint = StatusBlue,
                        iconBg = StatusBlueTint,
                        isSelected = selectedRole == "CAR_OWNER",
                        onClick = { selectedRole = "CAR_OWNER" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Repair Shop card
                    RoleCardRedesign(
                        title = "I am a Repair Shop",
                        subtitle = "Grow your business and manage customers",
                        icon = Icons.Default.Settings,
                        iconTint = OrangePrimary,
                        iconBg = OrangeSubtle,
                        isSelected = selectedRole == "SHOP_OWNER",
                        onClick = { selectedRole = "SHOP_OWNER" }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Continue button
                    PrimaryButton(
                        text = "Continue",
                        onClick = {
                            when (selectedRole) {
                                "CAR_OWNER" -> navController.navigate(Screen.CarOwnerAuth.route)
                                "SHOP_OWNER" -> navController.navigate("shop_reg_graph")
                            }
                        },
                        enabled = selectedRole != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Terms of Service Link
                    val annotatedText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextSubtle)) {
                            append("By continuing, you agree to our ")
                        }
                        pushStringAnnotation(tag = "TOS", annotation = "tos")
                        withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.SemiBold)) {
                            append("Terms of Service")
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "TOS", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    // Handle Terms click if required
                                }
                        }
                    )

                    // Repair Shop Sign In button (shown below Terms of Service when Shop Owner is selected)
                    if (selectedRole == "SHOP_OWNER") {
                        OutlinedButton(
                            onClick = { navController.navigate("shop_login_graph") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Sign In as Repair Shop",
                                style = MaterialTheme.typography.labelLarge,
                                color = OrangePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCardRedesign(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Selection animations
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else LightBorder,
        animationSpec = tween(250),
        label = "BorderColor"
    )
    val cardBgColor by animateColorAsState(
        targetValue = if (isSelected) OrangeSubtle else LightSurface,
        animationSpec = tween(250),
        label = "CardBackground"
    )

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large, // large = 16.dp
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Icon circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color = iconBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}