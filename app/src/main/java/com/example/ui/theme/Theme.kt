package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
    themeColors: ThemeColors = SunsetThemeColors,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = themeColors.background.toArgb()
                window.navigationBarColor = themeColors.cardBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !themeColors.isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !themeColors.isDark
            }
        }
    }

    val colorScheme = if (themeColors.isDark) {
        darkColorScheme(
            primary = themeColors.primaryAccent,
            onPrimary = themeColors.background,
            primaryContainer = themeColors.cardElevated,
            onPrimaryContainer = themeColors.textPrimary,
            secondary = themeColors.secondaryAccent,
            onSecondary = themeColors.background,
            secondaryContainer = themeColors.cardElevated,
            onSecondaryContainer = themeColors.textPrimary,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.cardBackground,
            onSurface = themeColors.textPrimary,
            surfaceVariant = themeColors.cardElevated,
            onSurfaceVariant = themeColors.textSecondary,
            outline = themeColors.cardBorder,
            error = ErrorRed,
            onError = themeColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = themeColors.primaryAccent,
            onPrimary = themeColors.cardBackground,
            primaryContainer = themeColors.cardElevated,
            onPrimaryContainer = themeColors.textPrimary,
            secondary = themeColors.secondaryAccent,
            onSecondary = themeColors.cardBackground,
            secondaryContainer = themeColors.cardElevated,
            onSecondaryContainer = themeColors.textPrimary,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.cardBackground,
            onSurface = themeColors.textPrimary,
            surfaceVariant = themeColors.cardElevated,
            onSurfaceVariant = themeColors.textSecondary,
            outline = themeColors.cardBorder,
            error = ErrorRed,
            onError = themeColors.cardBackground
        )
    }

    CompositionLocalProvider(LocalAppTheme provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
