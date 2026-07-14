package com.jayanth.jmoney.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val JmoneyColorScheme = ColorScheme(
    primary = Primary,
    primaryDim = Primary,
    onPrimary = Background,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Primary,
    secondary = CardBackground,
    onSecondary = Text,
    secondaryContainer = CardBackground,
    onSecondaryContainer = Text,
    tertiary = BorderColor,
    onTertiary = TextSecondary,
    tertiaryContainer = BorderColor,
    onTertiaryContainer = Text,
    surfaceContainerLow = CardBackground.copy(alpha = 0.7f),
    surfaceContainer = CardBackground,
    surfaceContainerHigh = CardBackground,
    onSurface = Text,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = BorderColor,
    background = Background,
    onBackground = Text,
    error = DangerColor,
    onError = Text,
    errorContainer = DangerColor.copy(alpha = 0.2f),
    onErrorContainer = DangerColor
)

@Composable
fun JmoneyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JmoneyColorScheme,
        content = content
    )
}