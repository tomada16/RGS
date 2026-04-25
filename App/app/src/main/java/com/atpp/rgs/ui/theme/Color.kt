package com.atpp.rgs.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta kolorów aplikacji — dwa poziomy:
 *
 *  1. **Brand tokens**     — surowe wartości HEX z brand-booka (źródło prawdy).
 *                            Nie używaj ich BEZPOŚREDNIO w UI — używaj ról
 *                            z `MaterialTheme.colorScheme` lub `MaterialTheme.rgsColors`.
 *
 *  2. **Shades / extras**  — warianty (light/dark) i pomocnicze odcienie.
 *                            Pozwalają rozszerzać paletę bez zmiany brandowych pięciu kolorów.
 *
 * Mapowanie tokenów -> ról M3 odbywa się w Theme.kt.
 */

// ============================================================
// Brand tokens (źródło: brand-book)
// ============================================================

/** Główne tło — matowa czerń. */
val BrandBlack = Color(0xFF0D0D0D)

/** Karty / panele — ciemny szary dający głębię nad tłem. */
val BrandPanel = Color(0xFF1C1C1C)

/** Akcent Gold — loga, przyciski akcji, podkreślenia. */
val BrandGold = Color(0xFFE2B044)

/** Akcent Red — głęboki burgund, drugorzędne akcje, elementy alarmowe. */
val BrandRed = Color(0xFF660000)

/** Tekst główny — czysta biel. */
val BrandWhite = Color(0xFFFFFFFF)

// Shades — warianty do dalszego rozwoju

/** Powierzchnia podniesiona o jeden poziom nad tłem (np. AppBar, sticky header). */
val BrandBlackElevated = Color(0xFF161616)

/** Powierzchnia nad panelem (dialog, bottom sheet, modal). */
val BrandPanelHigh = Color(0xFF252525)

/** Złoto rozjaśnione — hover/pressed na złotych przyciskach, gradient highlight. */
val BrandGoldLight = Color(0xFFEFC976)

/** Złoto przyciemnione — pressed/disabled state, cień pod złotem. */
val BrandGoldDark = Color(0xFFB58A2B)

/** Burgund rozjaśniony — najjaśniejszy stop w gradiencie przycisku, hover na czerwonych elementach. */
val BrandRedLight = Color(0xFFd11f1f)

/** Burgund przyciemniony — tło paneli formularza, ciemne końce gradientów. */
val BrandRedDark = Color(0xFF401919)

/** Tekst drugorzędny / opisowy — biel ze stłumioną jasnością. */
val BrandTextMuted = Color(0xFFB8B5AE)

/** Tekst nieaktywny / disabled. */
val BrandTextDisabled = Color(0xFF6B6B6B)

/** Linie podziału, ramki kart. */
val BrandDivider = Color(0xFF2A2A2A)

/** Obwódka pól formularza w stanie unfocused — chłodna szarość, dobrze widoczna na burgundzie. */
val BrandOutline = Color(0xFF464B53)

/** Półprzezroczysty scrim pod modalami (80% czerni). */
val BrandScrim = Color(0xCC000000)

