package com.carrepair.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Defines which type of banner to show
// Sealed class so no other type can be added outside this file accidentally
sealed class BannerType {
    object Success : BannerType()
    object Error : BannerType()
}

@Composable
fun MessageBanner(
    message: String,          // text to display
    bannerType: BannerType,   // controls color and icon
    visible: Boolean,         // controls whether banner is shown
    onDismiss: () -> Unit     // called when user taps X
) {

    // AnimatedVisibility shows/hides its content with animation
    // visible = true → runs enter animation
    // visible = false → runs exit animation, then removes from composition
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            // initialOffsetY returns where the slide starts
            // fullHeight is the pixel height of the component
            // positive value means starting below the screen — slides up into view
            initialOffsetY = { fullHeight -> fullHeight }
        ),
        exit = slideOutVertically(
            // negative value means sliding up out of view
            targetOffsetY = { fullHeight -> fullHeight }
        )
    ) {
        // Resolve colors based on banner type
        val backgroundColor = when (bannerType) {
            is BannerType.Success -> Color(0xFF1B5E20) // dark green
            is BannerType.Error -> Color(0xFFB71C1C)   // dark red
        }

        val icon = when (bannerType) {
            is BannerType.Success -> Icons.Filled.CheckCircle
            is BannerType.Error -> Icons.Filled.Warning
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left icon — success or error
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Message text — weight(1f) makes it take all remaining space
                // This pushes the close button to the far right
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
        }
    }
}