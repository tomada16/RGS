package com.atpp.rgs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material3 androidx.compose.material3.ColorScheme ma stały zbiór slotów
 * (primary, secondary, tertiary, error, surface...). Nasza marka ma jednak
 * kolory, które nie mapują się 1:1 — np. Gold pełni rolę akcentu akcji,
 * ale potrzebujemy też wariantów light/dark, koloru paneli wyższego poziomu,
 * tekstu drugorzędnego.
 *
 * Wystawiamy je jako osobny RgsExtraColors przez LocalRgsExtraColors]
 * dostępne w composable jako `MaterialTheme.rgsColors`.
 *
 * Dzięki temu:
 *  - dodanie nowego semantycznego koloru = jedno pole w data class,
 *  - kolory M3 pozostają zgodne ze standardem (działają z gotowymi komponentami).
 */
@Immutable
data class RgsExtraColors(
    // --- akcenty marki ---
    val accentGold: Color,
    val accentGoldLight: Color,
    val accentGoldDark: Color,
    val onAccentGold: Color,

    val accentRed: Color,
    val accentRedLight: Color,
    val accentRedDark: Color,
    val onAccentRed: Color,

    // --- powierzchnie ---
    val panel: Color,
    val panelHigh: Color,
    val divider: Color,
    val scrim: Color,

    // --- tekst ---
    val textPrimary: Color,
    val textMuted: Color,
    val textDisabled: Color,

)

/**
 * Domyślny zestaw — używany w RGSTheme. Wszystkie kolory pochodzą z Color.kt,
 * dzięki czemu zmiana brand-booka = edycja jednego pliku.
 */
internal val DefaultRgsExtraColors = RgsExtraColors(
    accentGold = BrandGold,
    accentGoldLight = BrandGoldLight,
    accentGoldDark = BrandGoldDark,
    onAccentGold = BrandBlack,

    accentRed = BrandRed,
    accentRedLight = BrandRedLight,
    accentRedDark = BrandRedDark,
    onAccentRed = BrandWhite,

    panel = BrandPanel,
    panelHigh = BrandPanelHigh,
    divider = BrandDivider,
    scrim = BrandScrim,

    textPrimary = BrandWhite,
    textMuted = BrandTextMuted,
    textDisabled = BrandTextDisabled

)

val LocalRgsExtraColors = staticCompositionLocalOf { DefaultRgsExtraColors }

/**
 * Skrót: `MaterialTheme.rgsColors.accentGold` zamiast `LocalRgsExtraColors.current.accentGold`.
 *
 * Naśladuje konwencję `MaterialTheme.colorScheme`.
 */
val MaterialTheme.rgsColors: RgsExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalRgsExtraColors.current
