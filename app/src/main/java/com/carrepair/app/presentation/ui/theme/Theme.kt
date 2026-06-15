package com.carrepair.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Spacing Constants
val ScreenPadding = 16.dp   // horizontal padding on all screens
val CardPadding   = 16.dp   // internal card padding
val SectionGap    = 16.dp   // between major sections
val ElementGap    = 8.dp    // between sibling elements

private val DarkColorScheme = darkColorScheme(
    primary          = OrangePrimary,
    onPrimary        = TextWhite,
    background       = NavyDark,
    onBackground     = TextWhite,
    surface          = NavyMedium,
    onSurface        = TextOffWhite,
    surfaceVariant   = NavyMedium,
    onSurfaceVariant = TextMuted,
    outline          = NavyBorder,
    error            = StatusRed,
    onError          = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary          = OrangePrimary,
    onPrimary        = TextWhite,
    background       = LightBackground,
    onBackground     = TextDark,
    surface          = LightSurface,
    onSurface        = TextDark,
    surfaceVariant   = LightSurface,
    onSurfaceVariant = TextSubtle,
    outline          = LightBorder,
    error            = StatusRed,
    onError          = TextWhite
)

private val RepaiiroShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chips, badges
    small      = RoundedCornerShape(10.dp),  // input fields
    medium     = RoundedCornerShape(12.dp),  // buttons
    large      = RoundedCornerShape(16.dp),  // cards
    extraLarge = RoundedCornerShape(50.dp)   // pills, avatars
)

@Composable
fun RepaiiroTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RepaiiroTypography,
        shapes      = RepaiiroShapes,
        content     = content
    )
}