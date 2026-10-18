package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
    background = DarkBackgroundBase,
    onBackground = TextPrimaryBase,
    surface = DarkSurfaceBase,
    onSurface = TextPrimaryBase,
    surfaceVariant = DarkCardBase,
    onSurfaceVariant = TextSecondaryBase,
    outline = DarkBorderBase,
    outlineVariant = OutlineStrong,
    error = DangerColor,
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = WildWatermelon,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = WildWatermelonContainer,
    onPrimaryContainer = WildWatermelonDim,
    secondary = ProteinColor,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = ProteinContainer,
    onSecondaryContainer = ProteinColor,
    tertiary = FatColor,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = FatContainer,
    onTertiaryContainer = FatColor,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = OutlineStrong,
    error = DangerColor,
    onError = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        "light" -> false
        "system" -> isSystemInDarkTheme()
        else -> true
    }

    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
