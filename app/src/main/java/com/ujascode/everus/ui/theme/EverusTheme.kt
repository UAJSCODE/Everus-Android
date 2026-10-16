package com.ujascode.everus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ujascode.everus.ui.design.*

private val DarkColorScheme = darkColorScheme(
    primary = EverusPrimary,
    secondary = EverusSecondary,
    tertiary = EverusAccent,
    background = EverusBackground,
    surface = EverusSurface,
    surfaceVariant = EverusElevatedSurface,
    onPrimary = Color(0xFF000000), // Black text on primary (for good contrast)
    onSecondary = Color(0xFFFFFFFF), // White text on secondary
    onTertiary = Color(0xFF000000), // Black text on accent
    onBackground = EverusPrimaryText, // Primary text on background
    onSurface = EverusPrimaryText, // Primary text on surface
    onSurfaceVariant = EverusSecondaryText,
    error = EverusError,
    onError = EverusBackground,
    outline = EverusBorder,
    outlineVariant = EverusBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightEverusPrimary,
    secondary = LightEverusSecondary,
    tertiary = LightEverusAccent,
    background = LightEverusBackground,
    surface = LightEverusSurface,
    surfaceVariant = Color(0xFFF2F3F5),
    onPrimary = Color(0xFFFFFFFF), // White text on primary
    onSecondary = Color(0xFF000000), // Black text on secondary
    onTertiary = Color(0xFFFFFFFF), // White text on accent
    onBackground = Color(0xFF000000), // Black text on background
    onSurface = Color(0xFF000000), // Black text on surface
    onSurfaceVariant = Color(0xFF52565E),
    error = LightEverusError,
    onError = Color(0xFFFFFFFF),
    outline = LightEverusBorder,
    outlineVariant = LightEverusBorder
)

@Composable
fun EverusTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EverusTypography,
        content = content
    )
}
