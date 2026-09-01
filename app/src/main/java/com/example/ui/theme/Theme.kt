package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FitCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFF9EEFFF),
    secondary = FitGreen,
    onSecondary = Color(0xFF003920),
    secondaryContainer = Color(0xFF005230),
    onSecondaryContainer = Color(0xFF86FBB7),
    tertiary = FitOrange,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF633F00),
    onTertiaryContainer = Color(0xFFFFDDB1),
    background = FitDarkBackground,
    onBackground = FitTextPrimary,
    surface = FitCardBackground,
    onSurface = FitTextPrimary,
    surfaceVariant = FitCardSurfaceVariant,
    onSurfaceVariant = FitTextSecondary,
    outline = FitCardBorder,
    error = FitRed,
    onError = Color.White
)

@Composable
fun FitTrackerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FitTrackerTheme(darkTheme, dynamicColor, content)
}

