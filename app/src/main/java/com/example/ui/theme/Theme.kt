package com.example.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Emerald800,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald950,
    secondary = Slate900,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate950,
    tertiary = Amber700,
    onTertiary = Color.White,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Slate950,
    background = SurfaceBackground,
    onBackground = Slate900,
    surface = SurfaceCard,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate900,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Slate50,
    surfaceContainerLowest = Color.White,
    outline = Slate400,
    outlineVariant = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HighContrastLightColorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Slate900
        ) {
            content()
        }
    }
}
