package com.jayanth.jmoney.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme
import androidx.wear.compose.material3.dynamicColorScheme

val JmoneyColorScheme = ColorScheme(
    primary = Primary,
    primaryDim = Primary,
    onPrimary = Background,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryContainer,
    onSecondary = OnSecondaryContainer,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = SuccessColor,
    onTertiary = OnTertiaryContainer,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    surfaceContainerLow = SecondaryContainer.copy(alpha = 0.7f),
    surfaceContainer = SecondaryContainer,
    surfaceContainerHigh = SecondaryContainer,
    onSurface = Text,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = BorderColor,
    background = Background,
    onBackground = Text,
    error = DangerColor,
    onError = Text,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun JmoneyTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val watchFaceColorScheme = dynamicColorScheme(context)

    MaterialTheme(
        colorScheme = watchFaceColorScheme ?: JmoneyColorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}