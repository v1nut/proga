package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark Theme Base Colors
val DarkBackgroundBase = Color(0xFF101014)
val DarkSurfaceBase = Color(0xFF18181F)
val DarkCardBase = Color(0xFF22222B)
val DarkCardElevatedBase = Color(0xFF2B2B37)
val DarkBorderBase = Color(0xFF2E2E3C)
val OutlineStrong = Color(0xFF6B6B80)

// Light Theme Base Colors
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFF1F5F9)
val LightCardElevated = Color(0xFFE2E8F0)
val LightBorder = Color(0xFFCBD5E1)

// Dynamic Theme Resolvers
val DarkBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val DarkCard: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val DarkCardElevated: Color
    @Composable get() = if (MaterialTheme.colorScheme.background == LightBackground) LightCardElevated else DarkCardElevatedBase

val DarkBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline

val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextTertiary: Color
    @Composable get() = if (MaterialTheme.colorScheme.background == LightBackground) LightTextTertiary else TextTertiaryBase

// Wild Watermelon Crayola (#FC6C85) - Primary Accent
val WildWatermelon = Color(0xFFFC6C85)
val WildWatermelonDim = Color(0xFFE25570)
val WildWatermelonContainer = Color(0x26FC6C85)

val Accent = WildWatermelon
val AccentDim = WildWatermelonDim
val AccentContainer = WildWatermelonContainer

// Water Balance Color
val WaterAqua = Color(0xFF06B6D4)
val WaterAquaContainer = Color(0x2606B6D4)

// Macronutrient Distinctive Colors
val ProteinColor = Color(0xFF38BDF8) // Vibrant Sky Blue
val ProteinContainer = Color(0x2638BDF8)

val FatColor = Color(0xFFFB923C) // Warm Amber Orange
val FatContainer = Color(0x26FB923C)

val CarbsColor = Color(0xFFC084FC) // Lilac Purple
val CarbsContainer = Color(0x26C084FC)

val CalorieColor = Color(0xFFFC6C85)

// Text & Status (Dark Theme)
val TextPrimaryBase = Color(0xFFF8FAFC)
val TextSecondaryBase = Color(0xFF94A3B8)
val TextTertiaryBase = Color(0xFF8E99AB) // Improved contrast (was #64748B)

// Text & Status (Light Theme)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextTertiary = Color(0xFF64748B)

val DangerColor = Color(0xFFF87171)
val SuccessColor = Color(0xFF4ADE80)
val WarningColor = Color(0xFFF59E0B) // Distinct warning color for over-consumption

// Banners
val WarningBannerBackground = Color(0xFF332615)
val WarningBannerText = Color(0xFFF59E0B)
val ErrorBannerBackground = Color(0xFF2A1517)
val ErrorBannerText = Color(0xFFF87171)
