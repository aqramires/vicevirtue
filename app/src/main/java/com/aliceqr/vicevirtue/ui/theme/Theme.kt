package com.aliceqr.vicevirtue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.aliceqr.vicevirtue.data.repository.ThemeMode

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
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

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

    // Radius
    val RadiusS = 8
    val RadiusM = 16
    val RadiusL = 24

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
