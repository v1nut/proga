package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WildWatermelon,
    onPrimary = Color(0xFF101014),
    primaryContainer = WildWatermelonContainer,
    onPrimaryContainer = WildWatermelon,
    secondary = ProteinColor,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = ProteinContainer,
    onSecondaryContainer = ProteinColor,
    tertiary = FatColor,
    onTertiary = Color(0xFF431407),
    tertiaryContainer = FatContainer,
    onTertiaryContainer = FatColor,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF262630),
    error = DangerColor,
    onError = Color(0xFF450A0A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
