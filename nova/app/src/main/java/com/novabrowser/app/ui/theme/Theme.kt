package com.novabrowser.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NovaDarkScheme = darkColorScheme(
    primary = NovaCoral,
    onPrimary = NovaInkBlack,
    secondary = NovaTeal,
    background = NovaInkBlack,
    onBackground = NovaFog100,
    surface = NovaGraphite900,
    onSurface = NovaFog100,
    surfaceVariant = NovaGraphite800,
    onSurfaceVariant = NovaFog200,
    outline = NovaGraphite600,
    error = NovaRed
)

private val NovaLightScheme = lightColorScheme(
    primary = NovaCoralDim,
    onPrimary = NovaWhite,
    secondary = NovaTeal,
    background = NovaSnow,
    onBackground = NovaGraphite900,
    surface = NovaWhite,
    onSurface = NovaGraphite900,
    surfaceVariant = NovaFog100,
    onSurfaceVariant = NovaGraphite400,
    outline = NovaFog200,
    error = NovaRed
)

@Composable
fun NovaBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NovaDarkScheme else NovaLightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NovaTypography,
        content = content
    )
}
