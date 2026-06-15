package com.carrepair.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carrepair.app.data.dto.payment.PaymentStatusResponseDto
import com.carrepair.app.presentation.components.*
import com.carrepair.app.presentation.ui.theme.*

@Composable
fun EscrowStatusCard(
    paymentStatus: PaymentStatusResponseDto,
    onConfirmJobComplete: (Long) -> Unit = {},
    onViewFullStatus: () -> Unit = {},
    isLoading: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = LightSurface,
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "Escrow",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                EscrowStatusChip(status = paymentStatus.escrowStatus)
            }

            // Amount
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    paymentStatus.currency,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
                Text(
                    "%,.0f".format(paymentStatus.amountTotal),
                    style = MaterialTheme.typography.headlineMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Countdown if applicable
            if (paymentStatus.escrowStatus == "FUNDS_RECEIVED" && paymentStatus.eligibleReleaseAt != null) {
                val countdown = computeCountdown(paymentStatus.eligibleReleaseAt)
                if (countdown != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, null, tint = TextSubtle, modifier = Modifier.size(14.dp))
                        Text(
                            "Auto-releases in $countdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle
                        )
                    }
                }
            }

            Divider(color = LightBorder)

            // Action buttons
            when (paymentStatus.escrowStatus) {
                "FUNDS_RECEIVED" -> {
                    OutlinedButton(
                        onClick = onViewFullStatus,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Text("View Full Status", color = TextDark, style = MaterialTheme.typography.labelMedium)
                    }
                }
                "RELEASED_TO_SHOP" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                        Text(
                            "Funds released to shop",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                else -> {
                    TextButton(
                        onClick = onViewFullStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Payment Details", color = OrangePrimary)
                    }
                }
            }

            // Dispute note
            Text(
                "${paymentStatus.disputeWindowHours}h dispute window after completion",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
