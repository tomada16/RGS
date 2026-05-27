package com.atpp.rgs.ui.misc

import androidx.compose.ui.graphics.Color
import com.atpp.rgs.R
import androidx.compose.ui.graphics.Brush

data class TableTheme(
    val bgResId: Int,
    val brandGold: Color,
    val brandGoldLight: Color,
    val buttonDark: Color,
    val buttonStay: Color
)

data class CrapsTheme(
    val bgResId: Int,
    val brandGold: Color,
    val brandGoldLight: Color,
    val buttonDark: Color,

    // --- WYGLĄD ZAKŁADÓW ---
    val passLineBg: Brush,
    val dontPassBg: Brush,

    // --- KOLORY KAFELKÓW HISTORII ---
    val historySevenBg: Color,
    val historyNaturalBg: Color,
    val historyCrapsBg: Color,
    val historyPointBg: Color
)

val CRAPS_THEME_REGISTRY = mapOf(
    "ocean_blue" to CrapsTheme(
        bgResId = R.drawable.ocean_craps_table_bg, // lub Twoje tło
        brandGold = Color(0xFFC5A059),
        brandGoldLight = Color(0xFFE8D099),
        buttonDark = Color(0xFF0A1128),
        passLineBg = Brush.verticalGradient(listOf(Color(0xFF003B73), Color(0xFF001F3D))),
        dontPassBg = Brush.verticalGradient(listOf(Color(0xFF1E2433), Color(0xFF0A1128))),

        historySevenBg = Color(0xFFC75A43),
        historyNaturalBg = Color(0xFFC5A059).copy(alpha = 0.75f),
        historyCrapsBg = Color(0xFF0A1128),
        historyPointBg = Color(0xFF003B73).copy(alpha = 0.45f)
    ),

    // --- NOWY: SOLAR FLARE (Słoneczny, bursztynowy) ---
    // Akcenty: Żywe, ciepłe złoto i mocne, rdzawe barwy.
    "solar_flare" to CrapsTheme(
        bgResId = R.drawable.solar_craps_table_bg, // np. solar_craps_bg
        brandGold = Color(0xFFE59400),      // Żywe, ciepłe pomarańczowe złoto
        brandGoldLight = Color(0xFFFFCB6B), // Jasny, słoneczny blask
        buttonDark = Color(0xFF2A1604),     // Bardzo ciemny, ciepły brąz
        passLineBg = Brush.verticalGradient(listOf(Color(0xFFB85E00), Color(0xFF7A3B00))), // Rdzawo-bursztynowy gradient
        dontPassBg = Brush.verticalGradient(listOf(Color(0xFF4A2600), Color(0xFF2A1604))), // Ciemna czekolada
        historySevenBg = Color(0xFFD9381E), // Mocna, ognista czerwień
        historyNaturalBg = Color(0xFFE59400).copy(alpha = 0.75f),
        historyCrapsBg = Color(0xFF2A1604),
        historyPointBg = Color(0xFFB85E00).copy(alpha = 0.45f)
    ),

    // --- NOWY: VELVET ROSE (Gotycki, bordowy) ---
    // Akcenty: Różowe złoto (Rose Gold) na tle głębokiego, ciemnego wina.
    "crimson_rose" to CrapsTheme(
        bgResId = R.drawable.rose_craps_table_bg, // np. rose_craps_bg
        brandGold = Color(0xFFDDA7A5),      // Eleganckie, zgaszone Różowe Złoto
        brandGoldLight = Color(0xFFFDF0F0), // Bardzo jasny, subtelny, chłodny róż
        buttonDark = Color(0xFF1A0B14),     // Ekstremalnie ciemna, niemal czarna śliwka/bakłażan
        passLineBg = Brush.verticalGradient(listOf(Color(0xFF6B1839), Color(0xFF38081C))), // Głębokie, aksamitne bordo
        dontPassBg = Brush.verticalGradient(listOf(Color(0xFF2E1122), Color(0xFF1A0B14))), // Mroczny fiolet
        historySevenBg = Color(0xFFC2183D), // Krwista, karmazynowa czerwień róży
        historyNaturalBg = Color(0xFFDDA7A5).copy(alpha = 0.75f),
        historyCrapsBg = Color(0xFF1A0B14),
        historyPointBg = Color(0xFF6B1839).copy(alpha = 0.45f)
    )
)

val THEME_REGISTRY = mapOf(
    // 1. EMERALD (Klasyk) -> Akcenty: Antyczny Mosiądz
    "emerald" to TableTheme(
        bgResId = R.drawable.bj_table_bg, // upewnij się, że nazwa pasuje do Twoich plików
        brandGold = Color(0xFFB89947),      // Ciepły, stonowany mosiądz
        brandGoldLight = Color(0xFFD4BB7E), // Jasny refleks mosiądzu
        buttonDark = Color(0xFF1A241A),     // Bardzo głęboka, ciemna zieleń (prawie czarna)
        buttonStay = Color(0xFF2E4C2E)      // Klasyczna zieleń leśna
    ),

    // 2. BURGUNDY (High Stakes) -> Akcenty: Szampańskie Złoto (Elegancja)
    "burgundy" to TableTheme(
        bgResId = R.drawable.burgundy_bj_table_bg,
        brandGold = Color(0xFFE2C29B),      // Delikatne, luksusowe szampańskie złoto
        brandGoldLight = Color(0xFFFFF5E6), // Prawie biały, bardzo jasny szampan do rozbłysków
        buttonDark = Color(0xFF241114),     // Ekstremalnie ciemny mahoń
        buttonStay = Color(0xFF5C1D24)      // Bogate, ciemne wino / bordo
    ),

    // 3. MIDNIGHT (Private Lounge) -> Akcenty: Księżycowe Srebro
    "midnight" to TableTheme(
        bgResId = R.drawable.midnight_bj_table_bg,
        brandGold = Color(0xFFE0E6ED),      // Chłodne, księżycowe srebro / platyna
        brandGoldLight = Color(0xFFFFFFFF), // Czysta biel
        buttonDark = Color(0xFF151522),     // Bardzo ciemny granat (Nocne niebo)
        buttonStay = Color(0xFF2B3B5A)      // Stalowy, chłodny błękit
    ),

    // 4. FIRE AND ICE -> Akcenty: Różowe Złoto / Miedź
    // (Ten gradient wymaga koloru, który zagra i z czerwienią na górze, i z fioletem na dole)
    "fireice" to TableTheme(
        bgResId = R.drawable.fireice_bj_table_bg,
        brandGold = Color(0xFFE0A96D),      // Luksusowa miedź / Różowe złoto
        brandGoldLight = Color(0xFFFFD3A3), // Jasna miedź
        buttonDark = Color(0xFF251122),     // Ciemna śliwka / głęboki fiolet
        buttonStay = Color(0xFF591C36)      // Zgaszony, purpurowy karmazyn
    ),

    // 5. ONYX (Ultimate VIP) -> Akcenty: Czyste 24-karatowe Złoto
    // (Na tle głębokiej czerni najlepiej wygląda klasyczne, jaskrawe złoto kasynowe)
    "onyx" to TableTheme(
        bgResId = R.drawable.onyx_bj_table_bg,
        brandGold = Color(0xFFD4AF37),      // Czyste, mocne złoto
        brandGoldLight = Color(0xFFF9E596), // Jasnożółte złoto
        buttonDark = Color(0xFF101010),     // Prawie czysty czarny
        buttonStay = Color(0xFF2A2A2A)      // Ciemny grafit, ekstremalnie minimalistyczny
    ),
)