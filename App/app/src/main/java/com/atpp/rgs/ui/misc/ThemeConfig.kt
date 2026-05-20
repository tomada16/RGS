package com.atpp.rgs.ui.misc

import androidx.compose.ui.graphics.Color
import com.atpp.rgs.R

data class TableTheme(
    val bgResId: Int,
    val brandGold: Color,
    val brandGoldLight: Color,
    val buttonDark: Color,
    val buttonStay: Color
)

val THEME_REGISTRY = mapOf(
    "emerald" to TableTheme(
        bgResId = R.drawable.bj_table_bg, // Upewnij się, że ta nazwa to Twój plik tła
        brandGold = Color(0xFFB8860B),
        brandGoldLight = Color(0xFFFFE066),
        buttonDark = Color(0xFF333333),
        buttonStay = Color(0xFF5C0000)
    ),

    "burgundy" to TableTheme(
        bgResId = R.drawable.burgundy_bj_table_bg,
        brandGold = Color(0xFFC0C0C0), // Np. Srebro dla stołu Burgundy!
        brandGoldLight = Color(0xFFFFFFFF),
        buttonDark = Color(0xFF221111),
        buttonStay = Color(0xFF8B0000)
    ),

    "midnight" to TableTheme(
        bgResId = R.drawable.midnight_bj_table_bg, // To jest ciemnoniebieski stół z gradientem
        brandGold = Color(0xFFA67C52),      // Bogaty, szczotkowany mosiądz dla głównych akcentów i tekstu
        brandGoldLight = Color(0xFFCDB79E), // Jaśniejszy mosiądz do cieniowania i podświetleń
        buttonDark = Color(0xFF101015),    // Głęboki, węglowy granat dla ciemnych tła przycisków
        buttonStay = Color(0xFF1B4D3E)     // Bogata, nasycona zieleń leśna dla przycisku "Stay"
    )
    // Tu będziesz w przyszłości dopisywał kolejne motywy!
)