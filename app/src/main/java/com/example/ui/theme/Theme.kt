package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DivineGold,
    onPrimary = MidnightBlue,
    primaryContainer = DivineGoldDark,
    onPrimaryContainer = PureIvory,
    secondary = HeavenlySky,
    onSecondary = MidnightBlue,
    secondaryContainer = CelestialBlue,
    onSecondaryContainer = PureIvory,
    tertiary = DivineGoldLight,
    onTertiary = MidnightBlue,
    background = DarkBackground,
    onBackground = PureIvory,
    surface = DarkSurface,
    onSurface = PureIvory,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SlateGray,
    error = StatusOverdue,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = PureIvory,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = DeepNavy,
    secondary = DivineGoldDark,
    onSecondary = PureIvory,
    secondaryContainer = DivineGoldLight.copy(alpha = 0.3f),
    onSecondaryContainer = DeepNavy,
    tertiary = CelestialBlue,
    onTertiary = PureIvory,
    background = LightBackground,
    onBackground = DeepNavy,
    surface = LightSurface,
    onSurface = DeepNavy,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DeepNavy.copy(alpha = 0.7f),
    error = StatusOverdue,
    onError = Color.White
)

@Composable
fun ZakarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
