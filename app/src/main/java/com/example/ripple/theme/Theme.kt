package com.example.ripple.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RippleColorScheme = darkColorScheme(
    primary = RippleCyan,
    onPrimary = OceanNight,
    primaryContainer = OceanSurfaceVariant,
    onPrimaryContainer = RippleCyan,
    secondary = RippleTeal,
    onSecondary = OceanNight,
    secondaryContainer = OceanSurfaceHover,
    onSecondaryContainer = RippleTeal,
    tertiary = RippleAqua,
    onTertiary = OceanNight,
    background = OceanNight,
    onBackground = TextPrimary,
    surface = OceanSurface,
    onSurface = TextPrimary,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = RippleCoral,
    onError = Color.White
)

@Composable
fun RippleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RippleColorScheme,
        typography = Typography,
        content = content
    )
}
