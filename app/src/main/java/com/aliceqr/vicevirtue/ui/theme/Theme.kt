package com.aliceqr.vicevirtue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary          = VirtueBlue,
    onPrimary        = NeutralWhite,
    primaryContainer = VirtueBlueSurface,
    onPrimaryContainer = VirtueBlueDark,

    secondary        = ViceRed,
    onSecondary      = NeutralWhite,
    secondaryContainer = ViceRedSurface,
    onSecondaryContainer = ViceRedDark,

    background       = NeutralWhite,
    onBackground     = NeutralBlack,
    surface          = NeutralSurface,
    onSurface        = NeutralBlack,
    surfaceVariant   = NeutralLight,
    onSurfaceVariant = NeutralMid,

    outline          = NeutralLight,
    error            = ViceRed,
    onError          = NeutralWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary          = VirtueBlueLight,
    onPrimary        = NeutralBlack,
    primaryContainer = DarkVirtueSurface,
    onPrimaryContainer = VirtueBlueLight,

    secondary        = ViceRedLight,
    onSecondary      = NeutralBlack,
    secondaryContainer = DarkViceSurface,
    onSecondaryContainer = ViceRedLight,

    background       = DarkBackground,
    onBackground     = NeutralWhite,
    surface          = DarkSurface,
    onSurface        = NeutralWhite,
    surfaceVariant   = DarkSurfaceAlt,
    onSurfaceVariant = NeutralGray,

    outline          = NeutralMid,
    error            = ViceRedLight,
    onError          = NeutralBlack,
)

@Composable
fun ViceVirtueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ViceVirtueTypography,
        shapes      = ViceVirtueShapes,
        content     = content
    )
}

object ViceVirtueTokens {
    // Spacing
    val SpaceXS  = 4
    val SpaceS   = 8
    val SpaceM   = 16
    val SpaceL   = 24
    val SpaceXL  = 32
    val SpaceXXL = 48

    // Elevation
    val ElevationCard   = 2
    val ElevationModal  = 8
    val ElevationFAB    = 6

    // Icon sizes
    val IconSm   = 20
    val IconMd   = 24
    val IconLg   = 40
    val IconHero = 64

    // Animation durations (ms)
    val AnimFast   = 150
    val AnimNormal = 300
    val AnimSlow   = 500
}
