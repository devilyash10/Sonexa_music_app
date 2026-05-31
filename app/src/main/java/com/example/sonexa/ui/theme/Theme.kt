package com.example.sonexa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Your custom Material 3 schema mapping values from Color.kt
private val SonexaDarkScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = OledBlack,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = NeonCyan,

    secondary = NeonPurple,
    onSecondary = TextPrimary,

    background = OledBlack,
    onBackground = TextPrimary,

    surface = DeepCharcoal,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = TextPrimary
)

// Default baseline fallback light scheme
private val SonexaLightScheme = lightColorScheme(
    primary = NeonCyan,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun SonexaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val amoledGold = Color(0xFFFFD700)

    val activeColorScheme = when {
        isAmoled -> darkColorScheme(
            background = Color.Black, // Pure black pixels for OLED panel power savings
            surface = Color.Black,
            surfaceVariant = Color(0xFF121212), // Deep elevated container surface
            primary = amoledGold,
            secondary = amoledGold,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
        // 🚨 FIXED: Correctly reference your customized design tokens here
        darkTheme -> SonexaDarkScheme
        else -> SonexaLightScheme
    }

    MaterialTheme(
        colorScheme = activeColorScheme,
        typography = Typography,
        content = content
    )
}