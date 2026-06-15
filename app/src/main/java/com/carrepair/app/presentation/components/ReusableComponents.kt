package com.carrepair.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carrepair.app.presentation.ui.theme.*

@Composable
fun StatusChip(label: String, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (label.uppercase().replace("_", " ")) {
        "PENDING", "OPEN" -> StatusAmberTint to StatusAmber
        "IN PROGRESS" -> StatusBlueTint to StatusBlue
        "COMPLETED" -> StatusGreenTint to StatusGreen
        "ACCEPTED" -> StatusGreenTint to StatusGreen
        "CANCELLED" -> LightBorder to TextSubtle
        "UNLOCKED" -> StatusGreenTint to StatusGreen
        "BODY DAMAGE" -> OrangeSubtle to OrangePrimary
        "MECHANICAL" -> StatusBlueTint to StatusBlue
        "PAINT" -> StatusAmberTint to StatusAmber
        "ELECTRICAL" -> StatusGreenTint to StatusGreen
        "INTERIOR" -> StatusBlueTint to StatusBlue
        else -> LightBorder to TextSubtle
    }
    Surface(
        color = bg,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier
    ) {
        Text(
            text = label.replace("_", " ").uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LeadStatusChip(status: String) {
    StatusChip(label = status)
}

@Composable
fun DarkNavHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
            
            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(offset * 1000f, 0f),
        end = Offset((offset + 1) * 1000f, 0f)
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(modifier = Modifier.background(brush))
    }
}
