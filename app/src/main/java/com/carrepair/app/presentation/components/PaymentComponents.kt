package com.carrepair.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carrepair.app.presentation.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ── Fee Row ───────────────────────────────────────────────────────────────────

@Composable
fun FeeRow(
    label: String,
    value: String,
    labelColor: Color = TextSubtle,
    valueColor: Color = TextDark,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = labelColor,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ── Escrow Status Chip ────────────────────────────────────────────────────────

@Composable
fun EscrowStatusChip(status: String, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (status) {
        "INITIATED"        -> Triple(StatusAmberTint, StatusAmber, "Initiated")
        "FUNDS_RECEIVED"   -> Triple(StatusGreenTint, StatusGreen, "Funds Secured")
        "RELEASED_TO_SHOP" -> Triple(StatusBlueTint, StatusBlue, "Released to Shop")
        "DISPUTED"         -> Triple(StatusRedTint, StatusRed, "Under Dispute")
        "REFUNDED_TO_OWNER"-> Triple(StatusAmberTint, StatusAmber, "Refunded")
        else               -> Triple(LightBorder, TextSubtle, status)
    }
    Surface(color = bg, shape = MaterialTheme.shapes.extraLarge, modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Animated Pulsing Ring ─────────────────────────────────────────────────────

@Composable
fun PulsingRing(color: Color, size: Dp = 80.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = (this.size.minDimension / 2) * scale,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
    )
}

// ── Secure Badge Row ──────────────────────────────────────────────────────────

@Composable
fun SecureBadgeRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Icons.Default.Lock to "256-bit SSL",
            Icons.Default.VerifiedUser to "PCI-DSS",
            Icons.Default.Shield to "Escrow Protected"
        ).forEachIndexed { i, (icon, text) ->
            if (i > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(14.dp)
                        .background(LightBorder)
                )
                Spacer(Modifier.width(10.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(12.dp))
                Text(text, style = MaterialTheme.typography.labelSmall, color = TextSubtle)
            }
            if (i < 2) Spacer(Modifier.width(10.dp))
        }
    }
}

// ── Escrow Timeline ───────────────────────────────────────────────────────────

data class TimelineStep(
    val label: String,
    val sublabel: String,
    val icon: ImageVector,
    val isCompleted: Boolean,
    val isActive: Boolean
)

@Composable
fun EscrowTimeline(escrowStatus: String, modifier: Modifier = Modifier) {
    val steps = listOf(
        TimelineStep(
            "Payment Initiated", "Transaction started",
            Icons.Default.PlayCircle,
            isCompleted = escrowStatus != "INITIATED",
            isActive = escrowStatus == "INITIATED"
        ),
        TimelineStep(
            "Funds Secured", "Held safely in escrow",
            Icons.Default.Lock,
            isCompleted = escrowStatus in listOf("RELEASED_TO_SHOP", "DISPUTED", "REFUNDED_TO_OWNER"),
            isActive = escrowStatus == "FUNDS_RECEIVED"
        ),
        TimelineStep(
            "Repair Complete", "Shop finished work",
            Icons.Default.Build,
            isCompleted = escrowStatus in listOf("RELEASED_TO_SHOP", "REFUNDED_TO_OWNER"),
            isActive = false
        ),
        TimelineStep(
            "Funds Released", "Shop payment sent",
            Icons.Default.CheckCircle,
            isCompleted = escrowStatus == "RELEASED_TO_SHOP",
            isActive = false
        )
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
        steps.forEachIndexed { index, step ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Line + icon column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    // Top connector line
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(12.dp)
                                .background(
                                    if (step.isCompleted || step.isActive) OrangePrimary
                                    else LightBorder
                                )
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }

                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isCompleted -> OrangePrimary
                                    step.isActive    -> OrangeSubtle
                                    else             -> LightBackground
                                }
                            )
                            .border(
                                1.5.dp,
                                when {
                                    step.isCompleted -> OrangePrimary
                                    step.isActive    -> OrangePrimary
                                    else             -> LightBorder
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                step.isCompleted -> Color.White
                                step.isActive    -> OrangePrimary
                                else             -> TextMuted
                            }
                        )
                    }

                    // Bottom connector line
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(12.dp)
                                .background(
                                    if (step.isCompleted) OrangePrimary else LightBorder
                                )
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // Text content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp, bottom = if (index < steps.lastIndex) 8.dp else 16.dp)
                ) {
                    Text(
                        text = step.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = when {
                            step.isCompleted -> TextDark
                            step.isActive    -> OrangePrimary
                            else             -> TextMuted
                        },
                        fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.SemiBold
                    )
                    Text(
                        text = step.sublabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (step.isActive || step.isCompleted) TextSubtle else TextMuted
                    )
                }
            }
        }
    }
}

// ── Countdown text ────────────────────────────────────────────────────────────

fun computeCountdown(eligibleReleaseAt: String?): String? {
    if (eligibleReleaseAt == null) return null
    return try {
        val target = LocalDateTime.parse(
            eligibleReleaseAt,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        val now = LocalDateTime.now()
        if (target.isBefore(now)) return "Eligible for release"
        val totalMinutes = ChronoUnit.MINUTES.between(now, target)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        "${hours}h ${minutes}m remaining"
    } catch (e: Exception) { null }
}
