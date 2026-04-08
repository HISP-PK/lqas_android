package org.dhis2.dqapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Accent,
    secondary = Accent2,
    background = Bg1,
    surface = Card,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    outline = Border
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = Accent2,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkInk,
    onSurface = DarkInk,
    outline = Color(0xFF2A3446)
)

@Composable
fun DQAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}

val AppBackgroundBrush = Brush.linearGradient(
    colors = listOf(Bg1, Bg2)
)
