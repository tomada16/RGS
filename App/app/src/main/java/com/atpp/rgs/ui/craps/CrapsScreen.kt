package com.atpp.rgs.ui.craps

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.blackjack.formatFullCurrency
import com.atpp.rgs.ui.misc.CrapsTheme
import com.atpp.rgs.ui.theme.BrandBlack
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import com.atpp.rgs.data.repository.UserRepository

// ─────────────────────────────────────────────────────────────────────────────
// Główny ekran gry Craps (Ujednolicony z Blackjackiem)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CrapsScreen(
    app: RgsApplication,
    userId: Int,
    onExit: () -> Unit,
    viewModel: CrapsViewModel = viewModel(factory = CrapsViewModel.factory(app, userId))
) {
    val state by viewModel.state.collectAsState()
    val theme by viewModel.currentTheme.collectAsState()
    val dicePrefix by viewModel.currentDicePrefix.collectAsState()

    var showWinLossOverlay by remember { mutableStateOf(false) }

    // NOWOŚĆ: Logika ukrywania UI (Żetony znikają w fazie POINT oraz podczas samego rzutu)
    val showBettingUI = !state.isRolling && state.roundResult == null && state.phase == CrapsPhase.COME_OUT

    // ZMIANA: Zawsze odejmujemy postawione pieniądze od portfela dla celów wizualnych
    val effectiveBalance = state.walletCoins - state.totalBetAmount

    LaunchedEffect(state.roundResult) {
        if (state.roundResult != null) {
            delay(1000)
            showWinLossOverlay = true
            delay(2000)
            showWinLossOverlay = false
            delay(200)
            viewModel.newRound()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = theme.bgResId),
            contentDescription = "Table Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ZMIANA: Zasilamy TopBar naszym kalkulowanym, bieżącym saldem
            TopCasinoBar(
                balance = effectiveBalance.toDouble(),
                theme = theme,
                onExit = onExit,
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 32.dp).padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RollHistoryBar(history = state.rollHistory, theme = theme)

            // --- NOWY STABILNY KONTENER NA STÓŁ Craps ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // KOSTKI: Wycentrowane, z dużym obszarem na lot
                DiceArea(
                    die1 = state.die1,
                    die2 = state.die2,
                    isRolling = state.isRolling,
                    hasRolled = state.hasRolled,
                    dicePrefix = dicePrefix,
                    modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp) // Przesunięte w górę, nad zakłady
                )

                // WSKAŹNIK PUNKTU: Przesunięty twardo na dół, dokładnie nad strefę zakładów.
                // Odsłania 100% górnej części stołu (Posejdona)!
                PointIndicator(
                    phase = state.phase,
                    point = state.point,
                    hasRolled = state.hasRolled,
                    theme = theme,
                    // Zmiana paddingu bottom na 170.dp, żeby bezpiecznie uciekł nad kafelki
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 170.dp)
                )

                // Sekcja zakładów (Wewnątrz Betting Line)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    BetSection(
                        tableBets     = state.tableBets,
                        bettingLocked = state.isRolling || state.roundResult != null || state.phase == CrapsPhase.POINT,
                        onPlaceBet    = viewModel::placeBet,
                        theme         = theme
                    )
                }
            }

            // --- DOLNE KONTROLKI Z ANIMACJĄ ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    // ZMIANA: Z 32.dp na 24.dp, żeby wcięcia zrównały się idealnie z kafelkami zakładów wyżej!
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                // Ukrywanie wyboru żetonów
                AnimatedVisibility(
                    visible = showBettingUI,
                    enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                    exit = fadeOut(tween(400)) + shrinkVertically(tween(400))
                ) {
                    ChipSelectorRow(
                        enabled = true,
                        baseWalletCoins = state.walletCoins, // ZMIANA: Żeby nominały żetonów nie znikały
                        effectiveBalance = effectiveBalance, // ZMIANA: Do wygaszania żetonów
                        selectedChipAmount = state.selectedChipAmount,
                        onChipSelect = viewModel::selectChip,
                        theme = theme
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CrapsBottomBar(
                    currentBet = state.totalBetAmount,
                    errorResId = state.errorResId,
                    canRoll = state.totalBetAmount > 0 && !state.isRolling && state.roundResult == null,
                    isRolling = state.isRolling,
                    showBettingUI = showBettingUI, // Przekazujemy logikę ukrywania
                    onUndo = viewModel::undoLastChip,
                    onRepeat = viewModel::repeatBet,
                    onClear = viewModel::clearBet,
                    onRoll = viewModel::roll,
                    theme = theme
                )
            }
        }

        AnimatedVisibility(
            visible = showWinLossOverlay,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.zIndex(2f)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))
                WinLossOverlay(gameState = state.roundResult, theme = theme)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UJEDNOLICONE KONTROLKI DOLNE (Klon z BJ)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CrapsBottomBar(
    currentBet: Int,
    errorResId: Int?,
    canRoll: Boolean,
    isRolling: Boolean,
    showBettingUI: Boolean,
    onUndo: () -> Unit,
    onRepeat: () -> Unit,
    onClear: () -> Unit,
    onRoll: () -> Unit,
    theme: CrapsTheme
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (errorResId != null) {
            Text(
                text = stringResource(errorResId),
                color = Color(0xFFFF6B6B),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 2.dp)
            )
        }

        AnimatedContent(
            targetState = showBettingUI,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
            label = "BottomBarTransition"
        ) { isBetting ->
            if (isBetting) {
                // FAZA COME OUT (Menu z przyciskami powtórki/kosza)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(95.dp).padding(end = 6.dp)) {
                        Text("CURRENT BET", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp)
                        Text("$${formatFullCurrency(currentBet.toDouble())}", color = theme.brandGold, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)).border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape).clickable { onRepeat() },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Refresh, contentDescription = "Powtórz", tint = Color.LightGray, modifier = Modifier.size(24.dp)) }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)).border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape).clickable { onClear() },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Delete, contentDescription = "Wyczyść", tint = Color.LightGray, modifier = Modifier.size(24.dp)) }

                        Spacer(modifier = Modifier.width(14.dp))

                        Button(
                            onClick = onRoll,
                            enabled = canRoll,
                            modifier = Modifier.height(56.dp).widthIn(min = 120.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.brandGold,
                                contentColor = Color(0xFF1A0F00),
                                disabledContainerColor = theme.buttonDark.copy(alpha = 0.35f),
                                disabledContentColor = Color(0xFF1A0F00).copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            // CZYSTY TEKST - BEZ IKONY
                            Text("ROLL", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.5.sp)
                        }
                    }
                }
            } else {
                // FAZA POINT / RZUTU (Ukryte żetony, wycentrowany przycisk)
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onRoll,
                        enabled = canRoll && !isRolling,
                        // ZMIANA: fillMaxWidth() sprawia, że zajmuje 100% miejsca i wyrównuje się z kafelkami!
                        modifier = Modifier.height(56.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.brandGold,
                            contentColor = Color(0xFF1A0F00),
                            disabledContainerColor = theme.buttonDark.copy(alpha = 0.35f),
                            disabledContentColor = Color(0xFF1A0F00).copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        // CZYSTY TEKST - BEZ IKONY
                        Text(if (isRolling) "ROLLING..." else "ROLL", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.5.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wybór żetonów (Ujednolicony z BJ: rozmiar, odstępy)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChipSelectorRow(
    enabled: Boolean,
    baseWalletCoins: Int, // Możesz to całkowicie usunąć z parametrów, jeśli chcesz posprzątać
    effectiveBalance: Int,
    selectedChipAmount: Int,
    onChipSelect: (Int) -> Unit,
    theme: CrapsTheme
) {
    // ZMIANA: Zamiast baseWalletCoins używamy effectiveBalance.
    // Dzięki temu żetony, na które nas nie stać, nie wejdą nawet do listy.
    val availableChips = getDynamicChipsCraps(effectiveBalance)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(if (enabled) 1f else 0.35f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableChips.forEach { chipValue ->
            val isSelected = chipValue == selectedChipAmount

            CasinoChip(
                value = chipValue,
                enabled = enabled, // ZMIANA: Usunięto dodatkowy warunek canAfford
                isSelected = isSelected,
                onClick = { if (enabled) onChipSelect(chipValue) },
                theme = theme
            )
        }
    }
}

@Composable
private fun CasinoChip(value: Int, enabled: Boolean, isSelected: Boolean, onClick: () -> Unit, theme: CrapsTheme) {
    val alpha = if (enabled) 1f else 0.35f
    Box(
        modifier = Modifier
            .size(54.dp) // Rozmiar 1:1 z Blackjackiem
            .clip(CircleShape)
            .background(Color.DarkGray.copy(alpha = alpha)) // Tło 1:1 z Blackjackiem
            .border(
                width = if (isSelected) 2.dp else 1.dp, // Grubsza ramka gdy wybrany
                color = theme.brandGold.copy(alpha = if (isSelected) 1f else alpha),
                shape = CircleShape
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatChipCraps(value),
            color = Color.White.copy(alpha = alpha),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp // Czcionka 1:1 z Blackjackiem
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wektorowe strefy zakładów (Inside Betting Lines)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Pozostałe komponenty UI (Dostosowane do motywów)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Historia rzutów (Uzależniona od motywu)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RollHistoryBar(history: List<RollHistoryItem>, theme: CrapsTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        history.forEach { item ->
            // KOLORY HISTORYCZNE Z NOWEGO CRApsTHEME
            val tileBgColor = when (item.total) {
                7 -> theme.historySevenBg
                11 -> theme.historyNaturalBg
                2, 3, 12 -> theme.historyCrapsBg
                else -> theme.historyPointBg
            }

            val tileBorder = when (item.total) {
                7 -> theme.historySevenBg.copy(alpha = 0.8f) // <--- Używamy teraz koloru z motywu!
                11 -> theme.brandGoldLight
                else -> theme.brandGold.copy(alpha = 0.2f)
            }

            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(30.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(tileBgColor)
                    .border(1.dp, tileBorder, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.total}",
                    color = if (item.total == 11) Color.Black else BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wektorowe strefy zakładów (Uzależnione od motywu)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BetSection(
    tableBets: Map<BetType, Int>,
    bettingLocked: Boolean,
    onPlaceBet: (BetType) -> Unit,
    theme: CrapsTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 12.dp)
    ) {
        // Animowane ukrywanie tekstu "PLACE YOUR BETS"
        AnimatedVisibility(
            visible = !bettingLocked,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "PLACE YOUR BETS",
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- PASS LINE ---
            val passBet = tableBets[BetType.PASS_LINE] ?: 0
            val passActive = passBet > 0

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp) // Zwiększona wysokość, by przyciski wyglądały potężniej
                    .clickable(enabled = !bettingLocked) { onPlaceBet(BetType.PASS_LINE) },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                shadowElevation = if (passActive) 8.dp else 2.dp // Dodany cień (Elevation)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (passActive) theme.passLineBg else SolidColor(Color.Black.copy(alpha = 0.4f)))
                        .border(
                            width = if (passActive) 2.dp else 1.dp,
                            color = if (passActive) theme.brandGold else theme.brandGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "PASS LINE",
                            color = if (passActive) BrandWhite else theme.brandGold.copy(alpha = 0.7f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.5.sp
                        )
                        if (passBet > 0) {
                            Spacer(Modifier.height(8.dp))
                            ActiveBetBadge(amount = passBet, theme = theme)
                        }
                    }
                }
            }

            // --- DON'T PASS BAR 12 ---
            val dontPassBet = tableBets[BetType.DONT_PASS] ?: 0
            val dontPassActive = dontPassBet > 0

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp) // Zwiększona wysokość
                    .clickable(enabled = !bettingLocked) { onPlaceBet(BetType.DONT_PASS) },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                shadowElevation = if (dontPassActive) 8.dp else 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (dontPassActive) theme.dontPassBg else SolidColor(Color.Black.copy(alpha = 0.4f)))
                        .border(
                            width = if (dontPassActive) 2.dp else 1.dp,
                            color = if (dontPassActive) theme.brandGoldLight.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "DON'T PASS",
                            color = if (dontPassActive) BrandWhite else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            "BAR 12",
                            color = if (dontPassActive) Color(0xFFFF8888) else Color(0xFFFF6B6B).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                        if (dontPassBet > 0) {
                            Spacer(Modifier.height(4.dp))
                            ActiveBetBadge(amount = dontPassBet, theme = theme)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Odświeżony Wskaźnik Punktu (Kasynowy "Puck")
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PointIndicator(
    phase: CrapsPhase,
    point: Int?,
    hasRolled: Boolean,
    theme: CrapsTheme,
    modifier: Modifier = Modifier // <--- DODANE
) {
    Box(
        modifier = modifier.height(84.dp), // <--- UŻYCIE MODYFIKATORA
        contentAlignment = Alignment.Center
    ) {
        if (phase == CrapsPhase.POINT && point != null) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = theme.buttonDark, // Ciemny, głęboki rdzeń motywu
                shadowElevation = 12.dp,  // Krążek wyraźnie odrywa się od tła
                border = BorderStroke(
                    width = 3.dp,
                    // Metaliczny, dwukolorowy złoty pierścień
                    brush = Brush.linearGradient(
                        colors = listOf(theme.brandGoldLight, theme.brandGold)
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Subtelna poświata wewnątrz krążka
                        .background(
                            Brush.radialGradient(
                                colors = listOf(theme.brandGold.copy(alpha = 0.15f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "POINT",
                            color = theme.brandGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$point",
                            color = BrandWhite, // Śnieżna biel dla potężnego kontrastu
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            lineHeight = 30.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Obszar kostek (Zaktualizowany do obsługi wektorów)
// ─────────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// Obszar kostek (Zaktualizowany dla fizyki 3D)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DiceArea(
    die1: Int,
    die2: Int,
    isRolling: Boolean,
    hasRolled: Boolean,
    dicePrefix: String,
    modifier: Modifier = Modifier // <--- DODANE
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth().height(180.dp) // <--- UŻYCIE MODYFIKATORA
    ) {
        if (hasRolled || isRolling) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp), // Więcej miejsca między kostkami
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Przekazujemy indeksy (0 i 1), żeby ich lot był asymetryczny i naturalny
                AnimatedRollingDie(
                    imageName = "${dicePrefix}_die_$die1",
                    isRolling = isRolling,
                    index = 0
                )
                AnimatedRollingDie(
                    imageName = "${dicePrefix}_die_$die2",
                    isRolling = isRolling,
                    index = 1
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NOWY KOMPONENT: Silnik Fizyki i Animacji 3D dla Kostki
// ─────────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// NOWY KOMPONENT: Animacja fizyczna "Heavy Roll" (Bez deformacji 3D)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnimatedRollingDie(
    imageName: String,
    isRolling: Boolean,
    index: Int
) {
    // Animables do kontrolowania lotu
    val translationY = remember { Animatable(0f) }
    val translationX = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val shadowElev = remember { Animatable(8f) }

    // Stan wymuszający zmianę obrazka (żeby uniknąć ładowania nanobananów/placeholderów)
    var currentImage by remember { mutableStateOf(imageName) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            val dir = if (index == 0) -1f else 1f

            // 1. Zaczynamy wyrzut (Szybkie wirowanie w płaszczyźnie płaskiej - bez deformacji!)
            launch {
                rotationZ.animateTo(
                    targetValue = rotationZ.value + (1080f * dir), // 3 pełne obroty
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
            }

            // 2. Trajektoria lotu: Łuk do góry i na boki
            launch {
                // Faza A: Wybicie w powietrze i rozjazd
                launch { translationY.animateTo(-250f, tween(300, easing = EaseOutCubic)) }
                launch { translationX.animateTo(80f * dir, tween(300, easing = EaseOutCubic)) }

                // Złudzenie bycia bliżej kamery
                launch { scale.animateTo(1.4f, tween(300, easing = EaseOutCubic)) }

                // Cień się odrywa i rozmywa
                launch { shadowElev.animateTo(32f, tween(300, easing = EaseOutCubic)) }

                delay(300)

                // Faza B: Opadanie pod ciężarem grawitacji
                launch {
                    translationY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy, // Sprężyste odbicie od stołu
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    translationX.animateTo(
                        targetValue = 40f * dir, // Kostki zostają lekko rozrzucone na boki
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 100f)
                    )
                }

                // Powrót do normalnego rozmiaru po uderzeniu
                launch { scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 200f)) }

                // Cień wraca do kości (ostrzeje)
                launch { shadowElev.animateTo(8f, spring(dampingRatio = 0.6f, stiffness = 200f)) }
            }
        } else {
            // Bezpieczna podmiana wyniku dopiero gdy kości leżą na stole (isRolling = false)
            currentImage = imageName
        }
    }

    // Bezpiecznik: Jeśli nie rzucamy, aktualizujemy obraz na wszelki wypadek
    LaunchedEffect(imageName, isRolling) {
        if (!isRolling) {
            currentImage = imageName
        }
    }

    // Rysowanie kości
    DieFaceAsset(
        imageName = if (isRolling) "${imageName.dropLast(1)}${(1..6).random()}" else currentImage, // Migotanie oczek w trakcie lotu
        size = 96.dp,
        shadowElevation = shadowElev.value.dp,
        modifier = Modifier.graphicsLayer {
            this.translationY = translationY.value
            this.translationX = translationX.value
            this.rotationZ = rotationZ.value
            this.scaleX = scale.value
            this.scaleY = scale.value
            // Usunięto całkowicie rotationX i rotationY - brak deformacji
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Czysty Wektorowy Asset Kości (Z dynamicznym cieniem)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DieFaceAsset(
    imageName: String,
    size: Dp,
    shadowElevation: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val drawableId = remember(imageName) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    Surface(
        modifier = modifier
            .size(size)
            .shadow(shadowElevation, RoundedCornerShape(18.dp)), // Dynamiczny cień odrywa kość od stołu!
        color = Color.Transparent
    ) {
        if (drawableId != 0) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Red, RoundedCornerShape(18.dp)))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UJEDNOLICONE KOMPONENTY UI Z BLACKJACKA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopCasinoBar(balance: Double, theme: CrapsTheme, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        // Usunięto twarde czarne tło i zbędne paddingi!
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Portfel (Saldo) - WRÓCIŁ NA LEWĄ STRONĘ (jak w BJ)
        Surface(color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = theme.brandGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = String.format(Locale.US, "%,d", balance.toInt()),
                    color = theme.brandGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }


        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable { onExit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, null, tint = Color.White)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UJEDNOLICONA NAKŁADKA WYNIKU (Wielki Tekst)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WinLossOverlay(gameState: RoundResult?, theme: CrapsTheme) {
    if (gameState == null) return

    // Tłumaczenie wyniku Craps na tekst z BJ
    val resultText = when (gameState) {
        RoundResult.WIN -> "YOU WIN!"
        RoundResult.LOSE -> "BUST!" // Zachowujemy dramatyzm z BJ
        RoundResult.PUSH -> "PUSH"
    }

    // Dynamiczny gradient z motywu stołu (jak w BJ)
    val fillGradient = Brush.verticalGradient(
        0.0f to theme.brandGoldLight,
        0.4f to theme.brandGold,
        1.0f to theme.brandGold
    )
    val outlineColor = Color(0xFF4A2E00) // Kontrastowy brąz do obwódki

    // Animacje skali
    val textScale = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            glowScale.animateTo(
                targetValue = 1.6f,
                animationSpec = tween(durationMillis = 600, easing = EaseOutExpo)
            )
        }
        launch {
            textScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(contentAlignment = Alignment.Center) {
        // Poświata (Złoto motywu)
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(glowScale.value)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.brandGold.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        // Wielki Tekst Wyniku (Z obwódką)
        Text(
            text = resultText,
            textAlign = TextAlign.Center,
            lineHeight = 72.sp,
            style = TextStyle(
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                // Obwódka
                drawStyle = Stroke(miter = 10f, width = 16f, join = StrokeJoin.Round),
                // Cień
                shadow = Shadow(color = Color.Black, offset = Offset(0f, 15f), blurRadius = 25f)
            ),
            color = outlineColor,
            modifier = Modifier.scale(textScale.value)
        )

        // Wypełnienie tekstu gradientem
        Text(
            text = resultText,
            textAlign = TextAlign.Center,
            lineHeight = 72.sp,
            style = TextStyle(
                brush = fillGradient,
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.scale(textScale.value)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pomocnicze
// ─────────────────────────────────────────────────────────────────────────────

fun formatChipCraps(value: Int): String {
    return when {
        value >= 1_000_000_000 -> "${(value / 1_000_000_000)}B"
        value >= 1_000_000 -> "${(value / 1_000_000)}M"
        value >= 1_000 -> "${(value / 1_000)}k"
        else -> value.toString()
    }
}

fun getDynamicChipsCraps(balance: Int): List<Int> {
    val CRAPS_CHIP_VALUES = listOf(
        10, 50, 100, 500,
        1_000, 5_000, 10_000, 50_000,
        100_000, 500_000, 1_000_000, 5_000_000,
        10_000_000, 50_000_000, 100_000_000, 500_000_000, 1_000_000_000
    )

    return CRAPS_CHIP_VALUES.filter { it <= balance }.takeLast(5)
}

@Composable
private fun ActiveBetBadge(amount: Int, theme: CrapsTheme, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, theme.brandGoldLight, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$${formatFullCurrency(amount.toDouble())}",
            color = theme.brandGoldLight,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
