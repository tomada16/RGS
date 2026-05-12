package com.atpp.rgs.ui.blackjack

import com.atpp.rgs.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // NOWY IMPORT DO CENTROWANIA
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.model.Card
import com.atpp.rgs.model.calculateScore
import com.atpp.rgs.ui.blackjack.BlackjackViewModel
import com.atpp.rgs.ui.blackjack.GameState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch

val GoldText = Color(0xFFFFD700)
val ButtonDarkGray = Color(0xFF333333)
val ButtonHit = Color.White
val ButtonStay = Color(0xFF5C0000)
val LuxuryGold = Color(0xFFFDB931)
val DeepGold = Color(0xFF9E7E38)

fun formatFullCurrency(value: Double): String {
    return when {
        value >= 1_000_000_000 -> String.format("%.2fB", value / 1_000_000_000.0)
        value >= 1_000_000 -> String.format("%.2fM", value / 1_000_000.0)
        value >= 1_000 -> String.format("%.1fk", value / 1_000.0)
        else -> value.toInt().toString()
    }
}

fun formatChip(value: Double): String {
    return when {
        value >= 1_000_000_000 -> "${(value / 1_000_000_000).toInt()}B"
        value >= 1_000_000 -> "${(value / 1_000_000).toInt()}M"
        value >= 1_000 -> "${(value / 1_000).toInt()}k"
        else -> value.toInt().toString()
    }
}

// NAPRAWIONE ŻETONY: Pełna lista wszystkich nominałów w grze.
// Algorytm po prostu bierze 5 największych, na które Cię stać.
val EXTENDED_CHIP_VALUES = listOf(
    10.0, 50.0, 100.0, 500.0,
    1_000.0, 5_000.0, 10_000.0, 50_000.0,
    100_000.0, 500_000.0, 1_000_000.0, 5_000_000.0,
    10_000_000.0, 50_000_000.0, 100_000_000.0, 500_000_000.0, 1_000_000_000.0
)

fun getDynamicChips(balance: Double): List<Double> {
    return EXTENDED_CHIP_VALUES.filter { it <= maxOf(balance, 10.0) }.takeLast(5)
}

@Composable
fun BlackjackScreen(onNavigateBack: () -> Unit, viewModel: BlackjackViewModel = viewModel()) {
    val playerHands by viewModel.playerHands.collectAsState()
    val activeHandIndex by viewModel.currentHandIndex.collectAsState()
    val dealerHand by viewModel.dealerHand.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val currentBet by viewModel.currentBet.collectAsState()

    // TIMER: Zmienna sterująca widocznością nakładki z wygraną
    var showOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(gameState) {
        if (gameState != GameState.ACTIVE && gameState != GameState.NOT_STARTED && gameState != GameState.DEALING) {
            // 1. ZATRZYMANIE: Czekamy 1000ms, aż krupier fizycznie odsłoni/dobierze ostatnią kartę
            delay(750)
            showOverlay = true

            // 2. KRÓTSZY CZAS: Napis wisi na ekranie tylko 1.5 sekundy (zamiast 2.5s)
            delay(1000)
            showOverlay = false
        } else {
            showOverlay = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Warstwa 1: TŁO
        Image(
            painter = painterResource(id = R.drawable.table_bg),
            contentDescription = "Tło stołu",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Warstwa 2: STÓŁ, KARTY, PRZYCISKI
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopCasinoBar(
                balance = balance,
                onExit = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Krupier u góry (zostawiamy 0.4f)
                Spacer(modifier = Modifier.weight(1f))

                DealerArea(dealerHand, gameState)

                // 2. ŚRODKOWA SPRĘŻYNA -> Zwiększona z 1f na 1.5f
                // (Puchnie mocniej, spychając gracza w dół ekranu)
                Spacer(modifier = Modifier.weight(1.5f))

                PlayerArea(playerHands, activeHandIndex)

                Spacer(modifier = Modifier.height(24.dp))

                PlacedBetArea(currentBet)

                // 3. DOLNA SPRĘŻYNA -> Zmniejszona z 0.3f na 0.05f
                // (Pozwala kartom gracza i żetonom opaść znacznie niżej, tuż nad kontrolki)
                Spacer(modifier = Modifier.weight(0.35f))

                BottomControls(
                    viewModel = viewModel,
                    gameState = gameState,
                    balance = balance,
                    playerHands = playerHands,
                    currentBet = currentBet,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                )
            }
        }

        // Warstwa 3: NAKŁADKA Z WYNIKIEM (Animowana, pojawia się na wierzchu i znika)
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(animationSpec = tween(300)), // Usunięte scaleIn() - tło ma tylko płynnie gasnąć!
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.zIndex(2f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Ściemnienie ekranu - pojawia się natychmiast bez efektu rosnącego kwadratu
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))

                WinLossOverlay(gameState)
            }
        }
    }
}

@Composable
fun WinLossOverlay(gameState: GameState) {
    val resultText = when (gameState) {
        GameState.PLAYER_WON -> "YOU WIN!"
        GameState.DEALER_WON -> "DEALER\nWINS"
        GameState.TIE -> "PUSH"
        GameState.PLAYER_BUSTED -> "BUST!"
        else -> ""
    }

    val fillGradient = Brush.verticalGradient(
        0.0f to Color(0xFFFFF7D6),
        0.4f to Color(0xFFFFD700),
        1.0f to Color(0xFFD4AF37)
    )
    val outlineColor = Color(0xFF4A2E00)

    // ROZDZIELONE ANIMACJE
    val textScale = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0f) }

    // Uruchamiamy dwie animacje jednocześnie
    LaunchedEffect(Unit) {
        // 1. Złota poświata "wybucha" na boki (szybka animacja Expo)
        launch {
            glowScale.animateTo(
                targetValue = 1.6f,
                animationSpec = tween(durationMillis = 600, easing = EaseOutExpo)
            )
        }
        // 2. Napis wskakuje z delikatnym efektem sprężyny
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

        // 1. RADIALNA POŚWIATA - Wycięta w okrąg i animowana
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(glowScale.value) // Rośnie od zera do 1.6x
                .clip(CircleShape)      // WYMUSZAMY idealny okrąg, zero kwadratowych kątów!
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x99FFD700), // Głęboki złoty blask w centrum
                            Color.Transparent  // Rozmycie do krawędzi
                        )
                    )
                )
        )

        // 2. WARSTWA OBRYSU I CIENIA 3D
        Text(
            text = resultText,
            textAlign = TextAlign.Center,
            lineHeight = 72.sp,
            style = LocalTextStyle.current.copy(
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                drawStyle = Stroke(
                    miter = 10f,
                    width = 16f,
                    join = StrokeJoin.Round
                ),
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 15f),
                    blurRadius = 25f
                )
            ),
            color = outlineColor,
            modifier = Modifier.scale(textScale.value) // Przypinamy animację tekstu
        )

        // 3. WARSTWA WYPEŁNIENIA Z GRADIENTEM
        Text(
            text = resultText,
            textAlign = TextAlign.Center,
            lineHeight = 72.sp,
            style = LocalTextStyle.current.copy(
                brush = fillGradient,
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.scale(textScale.value) // Przypinamy animację tekstu
        )
    }
}

@Composable
fun PlacedBetArea(betAmount: Double) {
    val visibilityAlpha = if (betAmount > 0) 1f else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(visibilityAlpha)
    ) {
        Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(50)) {
            Text(
                text = "BET: $${formatFullCurrency(betAmount)}",
                color = GoldText,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PlacedChipPlaceholder(betAmount)
    }
}

@Composable
fun PlacedChipPlaceholder(amount: Double) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color.DarkGray, CircleShape)
            .border(1.dp, GoldText, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(formatChip(amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun PlayerArea(hands: List<List<Card>>, activeHandIndex: Int) {
    val maxCards = hands.maxOfOrNull { it.size } ?: 1

    val scale = if (hands.size > 1) {
        (0.85f - (maxCards - 2) * 0.05f).coerceAtLeast(0.65f)
    } else 1f

    val handSpacing = if (maxCards > 3) (-20).dp else 16.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(handSpacing, Alignment.CenterHorizontally),
            modifier = Modifier
                .wrapContentWidth(unbounded = true)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            hands.forEachIndexed { index, hand ->
                key(index) {
                    val isActive = index == activeHandIndex
                    val handAlpha = if (isActive || hands.size == 1) 1f else 0.5f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .zIndex(if (isActive) 1f else 0f)
                            .alpha(handAlpha)
                    ) {
                        val scoreAlpha = if (hand.isNotEmpty()) 1f else 0f
                        ScoreIndicator(score = calculateScore(hand), alpha = scoreAlpha)

                        Spacer(modifier = Modifier.height(6.dp))

                        val cardSpread = 35f
                        val handWidth = 100.dp + (maxOf(0, hand.size - 1) * cardSpread).dp + 40.dp

                        Box(
                            modifier = Modifier
                                .width(handWidth)
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            hand.forEachIndexed { cardIndex, card ->
                                key(card.imageName) {
                                    val targetX = (cardIndex - (hand.size - 1) / 2f) * cardSpread
                                    val targetRotation = (cardIndex - (hand.size - 1) / 2f) * 12f

                                    val animatedX by animateFloatAsState(targetValue = targetX, animationSpec = tween(400, easing = EaseOutCubic), label = "")
                                    val animatedRotation by animateFloatAsState(targetValue = targetRotation, animationSpec = tween(400), label = "")

                                    var isVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(card.imageName) { isVisible = true }

                                    this@Column.AnimatedVisibility(
                                        visible = isVisible,
                                        enter = slideInVertically(initialOffsetY = { -1500 }, animationSpec = tween(500)) + fadeIn(),
                                        modifier = Modifier.offset(x = animatedX.dp).rotate(animatedRotation)
                                    ) {
                                        CardPlaceholder(card.imageName)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealerArea(hand: List<Card>, gameState: GameState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(140.dp).fillMaxWidth()
        ) {
            hand.forEachIndexed { index, card ->
                key(card.imageName) {
                    val targetX = (index - (hand.size - 1) / 2f) * 35f
                    val animatedX by animateFloatAsState(targetValue = targetX, animationSpec = tween(400, easing = EaseOutCubic), label = "")

                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(card.imageName) { isVisible = true }

                    val isHidden = (gameState == GameState.DEALING || gameState == GameState.ACTIVE) && index == 1

                    val flipRotation by animateFloatAsState(
                        targetValue = if (isHidden) 180f else 0f,
                        animationSpec = tween(500, easing = LinearOutSlowInEasing), label = ""
                    )

                    val imageName = if (flipRotation > 90f) "card_back" else card.imageName

                    this@Column.AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(initialOffsetY = { -1500 }, animationSpec = tween(500)) + fadeIn(),
                        modifier = Modifier.offset(x = animatedX.dp)
                    ) {
                        CardPlaceholder(
                            imageName = imageName,
                            modifier = Modifier.graphicsLayer {
                                rotationY = flipRotation
                                cameraDistance = 12f * density
                            }
                        )
                    }
                }
            }
        }

        var dealerVisibleScore by remember { mutableIntStateOf(0) }

        LaunchedEffect(hand, gameState) {
            if (gameState == GameState.ACTIVE || gameState == GameState.DEALING || gameState == GameState.NOT_STARTED) {
                dealerVisibleScore = if (hand.isNotEmpty()) calculateScore(listOf(hand.first())) else 0
            } else {
                delay(250)
                dealerVisibleScore = calculateScore(hand)
            }
        }

        val scoreAlpha = if (hand.isNotEmpty()) 1f else 0f
        Spacer(modifier = Modifier.height(8.dp))
        ScoreIndicator(score = dealerVisibleScore, alpha = scoreAlpha)
    }
}

@Composable
fun BottomControls(
    viewModel: BlackjackViewModel,
    gameState: GameState,
    balance: Double,
    playerHands: List<List<Card>>,
    currentBet: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        val availableChips = getDynamicChips(balance)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                availableChips.forEach { chipValue ->
                    ChipSelector(formatChip(chipValue)) { viewModel.placeBet(chipValue) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.repeatBet() }, modifier = Modifier.background(Color(0xFF2C2C2C), CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Powtórz", tint = Color.LightGray)
                }
                IconButton(onClick = { viewModel.clearBet() }, modifier = Modifier.background(Color(0xFF2C2C2C), CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Wyczyść", tint = Color.LightGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (gameState == GameState.ACTIVE || gameState == GameState.DEALING || gameState == GameState.DEALER_TURN) {
                val isActive = gameState == GameState.ACTIVE

                val canSplit = isActive &&
                        playerHands.size == 1 &&
                        playerHands[0].size == 2 &&
                        playerHands[0][0].rank == playerHands[0][1].rank &&
                        balance >= currentBet

                ActionButton("SPLIT", if(canSplit) ButtonDarkGray else Color.Transparent, if(canSplit) Color.White else Color.Gray, 1f) {
                    if(canSplit) viewModel.split()
                }
                Spacer(modifier = Modifier.width(8.dp))
                ActionButton("DOUBLE", if(isActive) ButtonDarkGray else Color.Transparent, Color.White, 1f) { if(isActive) viewModel.doubleDown() }
                Spacer(modifier = Modifier.width(8.dp))
                ActionButton("HIT", if(isActive) ButtonHit else ButtonDarkGray, if(isActive) Color.Black else Color.Gray, 1f) { if(isActive) viewModel.hit() }
                Spacer(modifier = Modifier.width(8.dp))
                ActionButton("STAND", if(isActive) ButtonStay else ButtonDarkGray, if(isActive) GoldText else Color.Gray, 1f) { if(isActive) viewModel.stand() }
            } else {
                ActionButton("DEAL", GoldText, Color.Black, 1f) { viewModel.startNewGame() }
            }
        }
    }
}

@Composable
fun TopCasinoBar(balance: Double, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50)) {
            Text("$${formatFullCurrency(balance)}", color = GoldText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        IconButton(onClick = onExit, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape).size(36.dp)) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
fun RowScope.ActionButton(text: String, color: Color, textColor: Color, weight: Float, onClick: () -> Unit) {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp), modifier = Modifier.height(64.dp).weight(weight), contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun ChipSelector(value: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(44.dp).background(Color.DarkGray, CircleShape).border(1.dp, GoldText, CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun CardPlaceholder(imageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawableId = remember(imageName) { context.resources.getIdentifier(imageName, "drawable", context.packageName) }

    Surface(
        modifier = modifier.size(width = 100.dp, height = 140.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        if (drawableId != 0) Image(painter = painterResource(id = drawableId), contentDescription = null, contentScale = ContentScale.Fit)
        else Box(modifier = Modifier.fillMaxSize().background(Color.Red))
    }
}

@Composable
fun ScoreIndicator(score: Int, alpha: Float) {
    Surface(
        color = GoldText,
        shape = RoundedCornerShape(50),
        modifier = Modifier.alpha(alpha)
    ) {
        Text(
            text = score.toString(),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}