package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF001A24),
    primaryContainer = Color(0xFF003848),
    onPrimaryContainer = Color(0xFF99E8FF),
    secondary = GreenBullish,
    onSecondary = Color(0xFF00220F),
    secondaryContainer = Color(0xFF003D1B),
    onSecondaryContainer = Color(0xFF75FFB4),
    tertiary = AmberWarning,
    onTertiary = Color(0xFF2E1900),
    error = RedBearish,
    onError = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
