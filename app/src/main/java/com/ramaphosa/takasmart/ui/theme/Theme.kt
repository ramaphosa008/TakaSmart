package com.ramaphosa.takasmart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.White

private val TakaColorScheme = lightColorScheme(
    primary             = Teal,
    onPrimary           = White,
    primaryContainer    = TealSurface,
    onPrimaryContainer  = TealDark,

    secondary           = Green,
    onSecondary         = White,
    secondaryContainer  = GreenSurface,
    onSecondaryContainer= GreenDark,

    tertiary            = Amber,
    onTertiary          = White,
    tertiaryContainer   = AmberSurface,
    onTertiaryContainer = AmberDark,

    error               = ErrorRed,
    onError             = White,
    errorContainer      = ErrorSurface,
    onErrorContainer    = ErrorDark,

    background          = GraySurface,
    onBackground        = GrayDark,
    surface             = White,
    onSurface           = GrayDark,
    surfaceVariant      = GreenSurface,
    onSurfaceVariant    = GrayMid,
    outline             = BorderColor,
)

@Composable
fun TakaSmartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TakaColorScheme,
        typography  = TakaTypography,
        content     = content
    )
}