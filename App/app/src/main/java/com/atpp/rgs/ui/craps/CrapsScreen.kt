package com.atpp.rgs.ui.craps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Zawiera statusBarsPadding i navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface // DODANY IMPORT
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.media.MediaAssets
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandGoldDark
import com.atpp.rgs.ui.theme.BrandTextMuted
import com.atpp.rgs.ui.theme.BrandWhite
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Główny ekran gry Craps
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CrapsScreen(
    app: RgsApplication,
    userId: Int,
    onExit: () -> Unit,
    viewModel: CrapsViewModel = viewModel(factory = CrapsViewModel.factory(app, userId))
) {
    val state by viewModel.state.collectAsState()

    // Obraz stołu z assets/images/games/crapsTable/craps_table_default.png
    val tableImage = MediaAssets.rememberAssetImage("games/crapsTable/craps_table_default.png")

    Box(modifier = Modifier.fillMaxSize()) {

        // ── 1. Tło pełnoekranowe: obrazek stołu ──────────────────────────
        if (tableImage != null) {
            Image(
                bitmap             = tableImage,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF8B1A1A), Color(0xFF3E0000))
                        )
                    )
            )
        }

        // Globalna nakładka — wyrównuje jasność obrazu z paskami UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55000000))
        )

        // ── 2. Warstwy UI ─────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // Górny pasek: saldo + przycisk wyjścia (Z ODSTĘPAMI JAK W BLACKJACKU)
            CrapsTopBar(
                coins = state.walletCoins,
                onExit = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Historia rzutów (fixed height — nie rusza rozmiaru obszaru gry)
            RollHistoryBar(history = state.rollHistory)

            // Obszar gry: wskaźnik punktu + kostki + zakłady
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PointIndicator(
                    phase     = state.phase,
                    point     = state.point,
                    hasRolled = state.hasRolled,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                DiceArea(
                    die1      = state.die1,
                    die2      = state.die2,
                    isRolling = state.isRolling,
                    hasRolled = state.hasRolled,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                BetSection(
                    currentBet    = state.currentBet,
                    chipHistory   = state.chipHistory,
                    bettingLocked = state.phase == CrapsPhase.POINT || state.isRolling
                )

                Spacer(Modifier.height(16.dp))
            }

            // Dolna strefa kontrolna (żetony + pasek) — Z ODSTĘPAMI JAK W BLACKJACKU
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                ChipSelectorRow(
                    enabled     = state.phase == CrapsPhase.COME_OUT
                            && !state.isRolling
                            && state.roundResult == null,
                    walletCoins = state.walletCoins,
                    currentBet  = state.currentBet,
                    onChipClick = viewModel::addChip
                )

                CrapsBottomBar(
                    currentBet  = state.currentBet,
                    errorResId  = state.errorResId,
                    canRoll     = state.currentBet > 0
                            && !state.isRolling
                            && state.roundResult == null,
                    canUndo     = state.chipHistory.isNotEmpty()
                            && state.phase == CrapsPhase.COME_OUT
                            && !state.isRolling,
                    onUndo      = viewModel::undoLastChip,
                    onRoll      = viewModel::roll
                )
            }
        }

        // ── Nakładka wyniku rundy (WIN / LOSE) ────────────────────────────
        state.roundResult?.let { result ->
            RoundResultOverlay(
                result     = result,
                amount     = state.lastBetAmount,
                onNewRound = viewModel::newRound
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Górny pasek - ZMODYFIKOWANY NA STYL BLACKJACKA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CrapsTopBar(coins: Int, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Portfel (lewa strona)
        Surface(color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.MonetizationOn,
                    contentDescription = stringResource(R.string.craps_cd_balance),
                    tint               = BrandGold,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = formatCoins(coins),
                    color      = BrandGold,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }
        }

        // Przycisk wyjścia X (Skopiowany w 100% ze stołu Blackjacka)
        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable { onExit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint               = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Historia rzutów (bez TABLE LIMIT)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RollHistoryBar(history: List<RollHistoryItem>) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            // Stała wysokość — obszar gry nie zmienia rozmiaru gdy pojawia się historia
            .height(42.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kafelki z wynikami ostatnich rzutów
        history.forEach { item ->
            val tileColor = when (item.total) {
                7        -> Color(0xFF8B0000)  // siódemka — ciemnoczerwony
                11       -> Color(0xFF7A6400)  // jedenastka — złotawy
                2, 3, 12 -> Color(0xFF5A0000)  // craps — burgundowy
                else     -> Color(0xFF2A2A2A)  // punkt — szary
            }
            Box(
                modifier         = Modifier
                    .padding(end = 6.dp)
                    .size(30.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(tileColor)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "${item.total}",
                    color      = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wskaźnik punktu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PointIndicator(
    phase: CrapsPhase,
    point: Int?,
    hasRolled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (phase == CrapsPhase.POINT && point != null) {
            // Złota moneta z numerem punktu
            Box(contentAlignment = Alignment.Center) {
                // Zewnętrzny pierścień
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB8860B))
                )
                // Wewnętrzny gradient
                Box(
                    modifier         = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFE066),
                                    Color(0xFFDAA520),
                                    Color(0xFFB8860B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text          = stringResource(R.string.craps_point_label),
                            color         = Color(0xFF3B2000),
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text       = "$point",
                            color      = Color(0xFF3B2000),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 24.sp,
                            lineHeight  = 26.sp
                        )
                    }
                }
            }
        } else if (!hasRolled) {
            // Przed pierwszym rzutem
            Text(
                text          = stringResource(R.string.craps_roll_to_start),
                color         = BrandGold.copy(alpha = 0.65f),
                fontWeight    = FontWeight.Bold,
                fontSize      = 12.sp,
                letterSpacing = 2.sp
            )
        }
        // Po instant-win/lose w COME_OUT: nakładka wyniku jest widoczna — nic tu nie pokazujemy
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Obszar kostek
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DiceArea(
    die1: Int,
    die2: Int,
    isRolling: Boolean,
    hasRolled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (hasRolled || isRolling) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                DieFace(value = die1, size = 88.dp)
                DieFace(value = die2, size = 88.dp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pojedyncza kostka (rysowana na Canvas)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DieFace(value: Int, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s  = this.size.width
        val cr = s * 0.16f   // promień zaokrąglenia rogów
        val dr = s * 0.085f  // promień oczka
        val p  = s * 0.235f  // padding oczka od krawędzi

        // Cień
        drawRoundRect(
            color        = Color(0x99000000),
            topLeft      = Offset(4f, 6f),
            size         = Size(s - 4f, s - 4f),
            cornerRadius = CornerRadius(cr)
        )

        // Tło kostki — gradient ciemnoczerwony
        drawRoundRect(
            brush        = Brush.linearGradient(
                colors = listOf(Color(0xFF9B1414), Color(0xFF3E0000)),
                start  = Offset(0f, 0f),
                end    = Offset(s, s)
            ),
            cornerRadius = CornerRadius(cr)
        )

        // Obramowanie
        drawRoundRect(
            color        = Color(0xFFBB3333),
            cornerRadius = CornerRadius(cr),
            style        = Stroke(width = 1.8f)
        )

        // Pozycje oczek
        val t = p; val b = s - p
        val l = p; val r = s - p
        val cx = s / 2f; val cy = s / 2f

        val dots: List<Offset> = when (value.coerceIn(1, 6)) {
            1 -> listOf(Offset(cx, cy))
            2 -> listOf(Offset(r, t), Offset(l, b))
            3 -> listOf(Offset(r, t), Offset(cx, cy), Offset(l, b))
            4 -> listOf(Offset(l, t), Offset(r, t), Offset(l, b), Offset(r, b))
            5 -> listOf(Offset(l, t), Offset(r, t), Offset(cx, cy), Offset(l, b), Offset(r, b))
            6 -> listOf(Offset(l, t), Offset(r, t), Offset(l, cy), Offset(r, cy), Offset(l, b), Offset(r, b))
            else -> emptyList()
        }

        dots.forEach { pos ->
            drawCircle(Color(0x55000000), dr + 1.5f, pos + Offset(1f, 1.5f))
            drawCircle(Color.White, dr, pos)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sekcja zakładów
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BetSection(
    currentBet: Int,
    chipHistory: List<Int>,
    bettingLocked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        // Nagłówek — wyraźny, biały
        Text(
            text          = stringResource(R.string.craps_place_your_bets),
            color         = BrandWhite,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 12.sp,
            letterSpacing = 2.sp,
            modifier      = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(10.dp))

        // PASS LINE — wycentrowany, węższa szerokość
        val passActive = currentBet > 0
        Box(
            modifier         = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { Box(
            modifier         = Modifier
                .fillMaxWidth(0.68f)
                .height(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (passActive)
                        Brush.linearGradient(listOf(Color(0xFFB8860B), Color(0xFF8B6400)))
                    else
                        Brush.linearGradient(listOf(Color(0xFF5A4200), Color(0xFF3A2A00)))
                )
                .border(
                    width = if (passActive) 2.dp else 1.dp,
                    color = if (passActive) BrandGold else BrandGold.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text          = stringResource(R.string.craps_bet_pass),
                    color         = BrandGold,
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 16.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text          = stringResource(R.string.craps_bet_line),
                    color         = BrandGold,
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 16.sp,
                    letterSpacing = 2.sp
                )
                if (currentBet > 0) {
                    Spacer(Modifier.height(3.dp))
                    ChipStackPreview(chipHistory)
                }
            }
        } } // koniec PASS LINE + zewnętrznego Box
    }
}

/** Miniaturowy rząd kółek reprezentujących ostatnio postawione żetony (maks. 4). */
@Composable
private fun ChipStackPreview(chipHistory: List<Int>) {
    Row(horizontalArrangement = Arrangement.Center) {
        chipHistory.takeLast(4).forEach { chip ->
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(chipColor(chip))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            )
            Spacer(Modifier.width(2.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wybór żetonów
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Wybór żetonów (DYNAMICZNE Z BLACKJACKA)
// ─────────────────────────────────────────────────────────────────────────────

val CRAPS_CHIP_VALUES = listOf(
    10, 50, 100, 500,
    1_000, 5_000, 10_000, 50_000,
    100_000, 500_000, 1_000_000, 5_000_000,
    10_000_000, 50_000_000, 100_000_000, 500_000_000, 1_000_000_000
)

fun getDynamicChipsCraps(balance: Int): List<Int> {
    return CRAPS_CHIP_VALUES.filter { it <= maxOf(balance, 10) }.takeLast(5)
}

fun formatChipCraps(value: Int): String {
    return when {
        value >= 1_000_000_000 -> "${(value / 1_000_000_000)}B"
        value >= 1_000_000 -> "${(value / 1_000_000)}M"
        value >= 1_000 -> "${(value / 1_000)}k"
        else -> value.toString()
    }
}

@Composable
private fun ChipSelectorRow(
    enabled: Boolean,
    walletCoins: Int,
    currentBet: Int,
    onChipClick: (Int) -> Unit
) {
    val availableChips = getDynamicChipsCraps(walletCoins)

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        availableChips.forEach { chipValue ->
            val chipEnabled = enabled && (currentBet + chipValue) <= walletCoins
            CasinoChip(
                value   = chipValue,
                enabled = chipEnabled,
                onClick = { onChipClick(chipValue) }
            )
        }
    }
}

@Composable
private fun CasinoChip(value: Int, enabled: Boolean, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.35f

    val baseColor = chipColor(value)
    val highlightColor = chipHighlightColor(value)

    Box(
        modifier         = Modifier
            .size(54.dp) // Zmniejszone, żeby 5 nominałów weszło w rząd bez ścisku
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        highlightColor.copy(alpha = alpha),
                        baseColor.copy(alpha = alpha)
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.25f * alpha), CircleShape)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = formatChipCraps(value),
            color      = Color.White.copy(alpha = alpha),
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 13.sp
        )
    }
}

/** Kolor bazowy żetonu */
private fun chipColor(amount: Int): Color = when {
    amount >= 1_000_000 -> Color(0xFF2A2A2A) // Miliony - czarne
    amount >= 1_000 -> Color(0xFFBB7799)     // Tysiące - różowe
    amount >= 100 -> Color(0xFF444444)       // Setki - ciemnoszare
    amount >= 25  -> Color(0xFF235A23)       // Zielone
    amount >= 5   -> Color(0xFF992222)       // Czerwone
    else          -> Color(0xFFB8860B)       // Złote
}

/** Kolor rozbłysku (środek gradientu w żetonie) */
private fun chipHighlightColor(amount: Int): Color = when {
    amount >= 1_000_000 -> Color(0xFF555555)
    amount >= 1_000 -> Color(0xFFFFBBDD)
    amount >= 100 -> Color(0xFF777777)
    amount >= 25  -> Color(0xFF449944)
    amount >= 5   -> Color(0xFFFF5555)
    else          -> Color(0xFFFFD700)
}

// ─────────────────────────────────────────────────────────────────────────────
// Dolny pasek akcji
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CrapsBottomBar(
    currentBet: Int,
    errorResId: Int?,
    canRoll: Boolean,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onRoll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Komunikat błędu
        if (errorResId != null) {
            Text(
                text     = stringResource(errorResId),
                color    = Color(0xFFFF6B6B),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 2.dp)
            )
        }

        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aktualny zakład (lewa strona)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text          = stringResource(R.string.craps_current_bet_label),
                    color         = BrandWhite.copy(alpha = 0.75f),
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text       = "\$$currentBet",
                    color      = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 24.sp
                )
            }

            // Przycisk Cofnij
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (canUndo) Color(0xFF3A0A0A) else Color(0xFF250404))
                    .then(if (canUndo) Modifier.clickable { onUndo() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Undo,
                    contentDescription = stringResource(R.string.craps_cd_undo),
                    tint               = if (canUndo) BrandWhite else BrandTextMuted,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            // Przycisk ROLL
            Button(
                onClick   = onRoll,
                enabled   = canRoll,
                modifier  = Modifier
                    .height(52.dp)
                    .widthIn(min = 130.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor         = BrandGold,
                    contentColor           = Color(0xFF1A0F00),
                    disabledContainerColor = BrandGoldDark.copy(alpha = 0.35f),
                    disabledContentColor   = Color(0xFF1A0F00).copy(alpha = 0.45f)
                ),
                shape     = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.Casino,
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text          = stringResource(R.string.craps_btn_roll),
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 16.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Nakładka wyniku rundy
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RoundResultOverlay(
    result: RoundResult,
    amount: Int,
    onNewRound: () -> Unit
) {
    val isWin = result == RoundResult.WIN

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .wrapContentHeight(),
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A00)),
            border = BorderStroke(
                width = 2.dp,
                color = if (isWin) BrandGold else Color(0xFF8B0000)
            )
        ) {
            Column(
                modifier            = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isWin) {
                    Text(
                        text       = stringResource(R.string.craps_result_win_title),
                        color      = BrandGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 42.sp,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text       = "+\$$amount",
                        color      = Color(0xFF66EE66),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 24.sp
                    )
                    Text(
                        text     = stringResource(R.string.craps_result_win_subtitle),
                        color    = BrandTextMuted,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text          = stringResource(R.string.craps_result_lose_title),
                        color         = Color(0xFFFF4444),
                        fontWeight    = FontWeight.ExtraBold,
                        fontSize      = 42.sp,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text       = "-\$$amount",
                        color      = Color(0xFFFF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 24.sp
                    )
                    Text(
                        text     = stringResource(R.string.craps_result_lose_subtitle),
                        color    = BrandTextMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick  = onNewRound,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BrandGold),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text          = stringResource(R.string.craps_btn_new_round),
                        color         = Color(0xFF1A0F00),
                        fontWeight    = FontWeight.ExtraBold,
                        fontSize      = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pomocnicze
// ─────────────────────────────────────────────────────────────────────────────

private val CrapsBg = Color(0xFF1A0000)

private fun formatCoins(coins: Int): String =
    "\$" + String.format(Locale.US, "%,d", coins)