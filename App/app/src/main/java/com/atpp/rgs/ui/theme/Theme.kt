package com.atpp.rgs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val RgsDarkColorScheme = darkColorScheme(
    // Akcent akcji — złoto na czarnym tle.
    primary = BrandGold,
    onPrimary = BrandBlack,
    primaryContainer = BrandGoldDark,
    onPrimaryContainer = BrandWhite,

    // Drugorzędny akcent — burgund.
    secondary = BrandRed,
    onSecondary = BrandWhite,
    secondaryContainer = BrandRedDark,
    onSecondaryContainer = BrandWhite,

    // Trzeci slot — wariant złota (np. tagi, podświetlenia).
    tertiary = BrandGoldLight,
    onTertiary = BrandBlack,
    tertiaryContainer = BrandGoldDark,
    onTertiaryContainer = BrandBlack,

    // Tło i powierzchnie.
    background = BrandBlack,
    onBackground = BrandWhite,

    surface = BrandPanel,
    onSurface = BrandWhite,
    surfaceVariant = BrandBlackElevated,
    onSurfaceVariant = BrandTextMuted,
    surfaceTint = BrandGold,

    inverseSurface = BrandWhite,
    inverseOnSurface = BrandBlack,
    inversePrimary = BrandGoldDark,

    // Błędy — używamy jaśniejszego burgundu (czysty 660000 byłby za ciemny do alertów).
    error = BrandRedLight,
    onError = BrandWhite,
    errorContainer = BrandRed,
    onErrorContainer = BrandWhite,

    // Linie i scrim.
    outline = BrandDivider,
    outlineVariant = BrandPanelHigh,
    scrim = BrandScrim
)

@Composable
fun RGSTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRgsExtraColors provides DefaultRgsExtraColors) {
        MaterialTheme(
            colorScheme = RgsDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
