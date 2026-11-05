package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KoroDarkColorScheme =
  darkColorScheme(
    primary = KoroCrimson,
    onPrimary = KoroTextPrimary,
    primaryContainer = KoroCrimsonDark,
    onPrimaryContainer = KoroTextPrimary,
    secondary = KoroGold,
    onSecondary = KoroBackground,
    tertiary = KoroCyan,
    onTertiary = KoroBackground,
    background = KoroBackground,
    onBackground = KoroTextPrimary,
    surface = KoroSurface,
    onSurface = KoroTextPrimary,
    surfaceVariant = KoroCardCharcoal,
    onSurfaceVariant = KoroTextSecondary,
    outline = KoroDivider,
  )

@Composable
fun KoroKoroTvTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = KoroDarkColorScheme, typography = Typography, content = content)
}
