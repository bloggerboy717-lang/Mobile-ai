package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.ThemePreset

data class ThemeColors(
    val preset: ThemePreset,
    val background: Color,
    val cardBackground: Color,
    val cardElevated: Color,
    val cardBorder: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val orbPrimary: Color,
    val orbSecondary: Color,
    val orbGlow: Color,
    val orbInner: Color,
    val isDark: Boolean
)

// 1. SUNSET THEME (Warm pastel peach · Coral & magenta twilight - from user screenshot)
val SunsetThemeColors = ThemeColors(
    preset = ThemePreset.SUNSET,
    background = Color(0xFFFFF6F0),
    cardBackground = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFFFF0E7),
    cardBorder = Color(0xFFF3D4C4),
    primaryAccent = Color(0xFFFF6B4A),     // Coral Flame button color as in screenshot Apply button
    secondaryAccent = Color(0xFFD946EF),   // Twilight magenta
    textPrimary = Color(0xFF381E19),
    textSecondary = Color(0xFF75493F),
    textMuted = Color(0xFFA1786D),
    orbPrimary = Color(0xFFFF6B4A),
    orbSecondary = Color(0xFFF43F5E),
    orbGlow = Color(0xFFFFA07A),
    orbInner = Color(0xFFFFE0D1),
    isDark = false
)

// 2. RED THEME (Obsidian crimson · Laser coral & flame)
val RedThemeColors = ThemeColors(
    preset = ThemePreset.RED,
    background = Color(0xFF15080C),
    cardBackground = Color(0xFF230D14),
    cardElevated = Color(0xFF32131D),
    cardBorder = Color(0xFF5A1E2E),
    primaryAccent = Color(0xFFFF2E55),     // Laser coral
    secondaryAccent = Color(0xFFFF6B4A),   // Flame
    textPrimary = Color(0xFFFFF0F3),
    textSecondary = Color(0xFFE5A6B4),
    textMuted = Color(0xFF9E6573),
    orbPrimary = Color(0xFFFF2E55),
    orbSecondary = Color(0xFFFF6B4A),
    orbGlow = Color(0xFFFF4D6D),
    orbInner = Color(0xFF3C0B17),
    isDark = true
)

// 3. LIGHT THEME (Crisp studio white · Blue & Violet accents)
val LightThemeColors = ThemeColors(
    preset = ThemePreset.LIGHT,
    background = Color(0xFFF8FAFC),
    cardBackground = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFF1F5F9),
    cardBorder = Color(0xFFE2E8F0),
    primaryAccent = Color(0xFF2563EB),     // Crisp blue
    secondaryAccent = Color(0xFF7C3AED),   // Violet
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    orbPrimary = Color(0xFF2563EB),
    orbSecondary = Color(0xFF7C3AED),
    orbGlow = Color(0xFF60A5FA),
    orbInner = Color(0xFFDBEAFE),
    isDark = false
)

// 4. DARK THEME (Deep midnight cosmic · Neon cyan & purple)
val DarkThemeColors = ThemeColors(
    preset = ThemePreset.DARK,
    background = Color(0xFF090D1F),
    cardBackground = Color(0xFF131A3D),
    cardElevated = Color(0xFF1A234E),
    cardBorder = Color(0xFF2A3772),
    primaryAccent = Color(0xFF38BDF8),     // Neon cyan
    secondaryAccent = Color(0xFFA855F7),   // Neon purple
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    orbPrimary = Color(0xFF38BDF8),
    orbSecondary = Color(0xFFA855F7),
    orbGlow = Color(0xFF818CF8),
    orbInner = Color(0xFF0D122D),
    isDark = true
)

// 5. EMERALD THEME (Obsidian jade · Neon mint & matrix green)
val EmeraldThemeColors = ThemeColors(
    preset = ThemePreset.EMERALD,
    background = Color(0xFF06140F),
    cardBackground = Color(0xFF0D251D),
    cardElevated = Color(0xFF13362A),
    cardBorder = Color(0xFF1E5240),
    primaryAccent = Color(0xFF10B981),     // Emerald
    secondaryAccent = Color(0xFF34D399),   // Mint
    textPrimary = Color(0xFFECFDF5),
    textSecondary = Color(0xFFA7F3D0),
    textMuted = Color(0xFF6EE7B7),
    orbPrimary = Color(0xFF10B981),
    orbSecondary = Color(0xFF34D399),
    orbGlow = Color(0xFF059669),
    orbInner = Color(0xFF082218),
    isDark = true
)

/**
 * Creates dynamic colors for any custom background HEX
 */
fun createCustomThemeColors(customHex: String): ThemeColors {
    val baseColor = try {
        val clean = customHex.removePrefix("#")
        val colorInt = clean.toLong(16).toInt() or (0xFF shl 24)
        Color(colorInt)
    } catch (e: Exception) {
        Color(0xFFFFF6F0)
    }

    val isDark = baseColor.luminance() < 0.45f

    return if (isDark) {
        ThemeColors(
            preset = ThemePreset.CUSTOM,
            background = baseColor,
            cardBackground = Color(
                red = (baseColor.red * 1.35f).coerceAtMost(1f),
                green = (baseColor.green * 1.35f).coerceAtMost(1f),
                blue = (baseColor.blue * 1.35f).coerceAtMost(1f)
            ),
            cardElevated = Color(
                red = (baseColor.red * 1.6f).coerceAtMost(1f),
                green = (baseColor.green * 1.6f).coerceAtMost(1f),
                blue = (baseColor.blue * 1.6f).coerceAtMost(1f)
            ),
            cardBorder = Color(
                red = (baseColor.red * 2.1f).coerceAtMost(1f),
                green = (baseColor.green * 2.1f).coerceAtMost(1f),
                blue = (baseColor.blue * 2.1f).coerceAtMost(1f)
            ),
            primaryAccent = Color(0xFFFF6B4A),
            secondaryAccent = Color(0xFFD946EF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFCBD5E1),
            textMuted = Color(0xFF94A3B8),
            orbPrimary = Color(0xFFFF6B4A),
            orbSecondary = Color(0xFFD946EF),
            orbGlow = Color(0xFFFFA07A),
            orbInner = baseColor,
            isDark = true
        )
    } else {
        ThemeColors(
            preset = ThemePreset.CUSTOM,
            background = baseColor,
            cardBackground = Color(0xFFFFFFFF),
            cardElevated = Color(
                red = (baseColor.red * 0.95f).coerceAtLeast(0f),
                green = (baseColor.green * 0.95f).coerceAtLeast(0f),
                blue = (baseColor.blue * 0.95f).coerceAtLeast(0f)
            ),
            cardBorder = Color(
                red = (baseColor.red * 0.85f).coerceAtLeast(0f),
                green = (baseColor.green * 0.85f).coerceAtLeast(0f),
                blue = (baseColor.blue * 0.85f).coerceAtLeast(0f)
            ),
            primaryAccent = Color(0xFFFF6B4A),
            secondaryAccent = Color(0xFFE11D48),
            textPrimary = Color(0xFF2C1810),
            textSecondary = Color(0xFF6B4337),
            textMuted = Color(0xFF946E62),
            orbPrimary = Color(0xFFFF6B4A),
            orbSecondary = Color(0xFFF43F5E),
            orbGlow = Color(0xFFFFA07A),
            orbInner = baseColor,
            isDark = false
        )
    }
}

fun resolveThemeColors(preset: ThemePreset, customHex: String): ThemeColors {
    return when (preset) {
        ThemePreset.SUNSET -> SunsetThemeColors
        ThemePreset.RED -> RedThemeColors
        ThemePreset.LIGHT -> LightThemeColors
        ThemePreset.DARK -> DarkThemeColors
        ThemePreset.EMERALD -> EmeraldThemeColors
        ThemePreset.CUSTOM -> createCustomThemeColors(customHex)
    }
}

val LocalAppTheme = staticCompositionLocalOf { SunsetThemeColors }

object AppTheme {
    val colors: ThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTheme.current
}
