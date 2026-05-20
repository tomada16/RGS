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
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material.icons.filled.MonetizationOn
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.misc.TableTheme

// Uniwersalny, biały przycisk (nie zależy od motywu stołu)
val ButtonHit = Color.White

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
fun BlackjackScreen(
    app: RgsApplication,
    userId: Int,
    onNavigateBack: () -> Unit,
    viewModel: BlackjackViewModel = viewModel(factory = BlackjackViewModel.factory(app, userId))
) {
    val playerHands by viewModel.playerHands.collectAsState()
    val activeHandIndex by viewModel.currentHandIndex.collectAsState()
    val dealerHand by viewModel.dealerHand.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val currentBet by viewModel.currentBet.collectAsState()

    // POBIERANIE MOTYWU Z VIEWMODELU
    val theme by viewModel.currentTheme.collectAsState()

    var showOverlay by remember { mutableStateOf(false) }
    var showBettingUI by remember { mutableStateOf(true) }

    LaunchedEffect(gameState) {
        when (gameState) {
            GameState.NOT_STARTED -> {
                showOverlay = false
                showBettingUI = true
            }
            GameState.DEALING, GameState.ACTIVE, GameState.DEALER_TURN -> {
                showOverlay = false
                showBettingUI = false
            }
            else -> {
                delay(750)
                showOverlay = true
                delay(1200)
                showOverlay = false
                delay(200)
                showBettingUI = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // DYNAMICZNE TŁO ZE SŁOWNIKA
        Image(
            painter = painterResource(id = theme.bgResId),
            contentDescription = "Tło stołu",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopCasinoBar(
                balance = balance,
                theme = theme,
                onExit = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp)
            )

            // --- NOWY STABILNY KONTENER NA STÓŁ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Zajmuje całą wolną przestrzeń między top barem a kontrolkami
            ) {
                // KRUPIER: Sztywno przyklejony do góry. Nigdy nie drgnie!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 94.dp), // Stały odstęp od górnego paska
                    contentAlignment = Alignment.TopCenter
                ) {
                    DealerArea(dealerHand, gameState, theme)
                }

                // GRACZ: Przyklejony do dołu. Porusza się gładko razem z dolnym menu.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 46.dp), // Odstęp od dolnych kontrolek
                    contentAlignment = Alignment.BottomCenter
                ) {
                    PlayerArea(playerHands, activeHandIndex, theme)
                }
            }

            // KONTROLKI
            BottomControls(
                viewModel = viewModel,
                gameState = gameState,
                balance = balance,
                playerHands = playerHands,
                currentBet = currentBet,
                showBettingUI = showBettingUI,
                theme = theme,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
            )
        }


        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.zIndex(2f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))
                WinLossOverlay(gameState, theme)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KONTROLKI DOLNE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BottomControls(
    viewModel: BlackjackViewModel,
    gameState: GameState,
    balance: Double,
    playerHands: List<List<Card>>,
    currentBet: Double,
    showBettingUI: Boolean,
    theme: TableTheme,
    modifier: Modifier = Modifier
) {
    val canBet = showBettingUI
    val availableChips = getDynamicChips(balance)

    Column(modifier = modifier.fillMaxWidth()) {

        this@Column.AnimatedVisibility(
            visible = showBettingUI,
            enter = fadeIn(tween(400)) + expandVertically(tween(400)),
            exit = fadeOut(tween(400)) + shrinkVertically(tween(400))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                availableChips.forEach { chipValue ->
                    ChipSelector(
                        value = formatChip(chipValue),
                        enabled = canBet,
                        theme = theme
                    ) {
                        viewModel.placeBet(chipValue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            this@Row.AnimatedVisibility(
                visible = showBettingUI,
                enter = fadeIn(tween(400)) + expandHorizontally(tween(400)),
                exit = fadeOut(tween(400)) + shrinkHorizontally(tween(400))
            ) {
                Column(modifier = Modifier.width(95.dp).padding(end = 6.dp)) {
                    Text(
                        text          = "CURRENT BET",
                        color         = Color.White.copy(alpha = 0.6f),
                        fontWeight    = FontWeight.SemiBold,
                        fontSize      = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text       = "$${formatFullCurrency(currentBet)}",
                        color      = theme.brandGold,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp
                    )
                }
            }

            AnimatedContent(
                targetState = showBettingUI,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                modifier = Modifier.weight(1f),
                label = "BottomBarTransition"
            ) { isBetting ->
                if (isBetting) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                .clickable { viewModel.repeatBet() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Powtórz", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                .clickable { viewModel.clearBet() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Wyczyść", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        val canDeal = currentBet > 0
                        Button(
                            onClick   = { viewModel.startNewGame() },
                            enabled   = canDeal,
                            modifier  = Modifier
                                .height(56.dp)
                                .widthIn(min = 120.dp),
                            colors    = ButtonDefaults.buttonColors(
                                containerColor         = theme.brandGold,
                                contentColor           = Color(0xFF1A0F00),
                                disabledContainerColor = theme.buttonDark.copy(alpha = 0.35f),
                                disabledContentColor   = Color(0xFF1A0F00).copy(alpha = 0.45f)
                            ),
                            shape     = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text          = "DEAL",
                                fontWeight    = FontWeight.ExtraBold,
                                fontSize      = 16.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                } else {
                    val isActive = gameState == GameState.ACTIVE

                    val hasOneHandWithTwoCards = playerHands.size == 1 && playerHands.getOrNull(0)?.size == 2
                    val isSplittable = hasOneHandWithTwoCards && playerHands[0][0].rank == playerHands[0][1].rank

                    val actualCanSplit = isSplittable && balance >= currentBet
                    val actualCanDouble = hasOneHandWithTwoCards && balance >= currentBet

                    val splitWeight by animateFloatAsState(targetValue = if (actualCanSplit) 1f else 0f, animationSpec = tween(400), label = "splitWeight")
                    val doubleWeight by animateFloatAsState(targetValue = if (actualCanDouble) 1f else 0f, animationSpec = tween(400), label = "doubleWeight")

                    Box(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        this@Row.AnimatedVisibility(
                            visible = isActive,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
                            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (splitWeight > 0.01f) {
                                    ActionButton(
                                        text = "SPLIT", color = theme.buttonDark, textColor = Color.White, enabled = isActive,
                                        modifier = Modifier.weight(splitWeight).alpha(splitWeight.coerceIn(0f, 1f))
                                    ) { viewModel.split() }
                                }
                                if (doubleWeight > 0.01f) {
                                    ActionButton(
                                        text = "DOUBLE", color = theme.buttonDark, textColor = Color.White, enabled = isActive,
                                        modifier = Modifier.weight(doubleWeight).alpha(doubleWeight.coerceIn(0f, 1f))
                                    ) { viewModel.doubleDown() }
                                }

                                ActionButton(
                                    text = "HIT", color = ButtonHit, textColor = Color.Black, enabled = isActive,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.hit() }

                                ActionButton(
                                    text = "STAND", color = theme.buttonStay, textColor = theme.brandGold, enabled = isActive,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.stand() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    color: Color,
    textColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(56.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ChipSelector(value: String, enabled: Boolean, theme: TableTheme, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.35f
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.DarkGray.copy(alpha = alpha))
            .border(1.dp, theme.brandGold.copy(alpha = alpha), CircleShape)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(value, color = Color.White.copy(alpha = alpha), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// POZOSTAŁE KOMPONENTY UI
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TopCasinoBar(balance: Double, theme: TableTheme, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Surface(color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint               = theme.brandGold,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "$${formatFullCurrency(balance)}",
                    color      = theme.brandGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable { onExit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, null, tint = Color.White)
        }
    }
}

@Composable
fun WinLossOverlay(gameState: GameState, theme: TableTheme) {
    val resultText = when (gameState) {
        GameState.PLAYER_WON -> "YOU WIN!"
        GameState.DEALER_WON -> "DEALER\nWINS"
        GameState.TIE -> "PUSH"
        GameState.PLAYER_BUSTED -> "BUST!"
        else -> ""
    }

    val fillGradient = Brush.verticalGradient(
        0.0f to Color(0xFFFFF7D6),
        0.4f to theme.brandGoldLight,
        1.0f to theme.brandGold
    )
    val outlineColor = Color(0xFF4A2E00)

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

        Text(
            text = resultText,
            textAlign = TextAlign.Center,
            lineHeight = 72.sp,
            style = LocalTextStyle.current.copy(
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                drawStyle = Stroke(miter = 10f, width = 16f, join = StrokeJoin.Round),
                shadow = Shadow(color = Color.Black, offset = Offset(0f, 15f), blurRadius = 25f)
            ),
            color = outlineColor,
            modifier = Modifier.scale(textScale.value)
        )

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
            modifier = Modifier.scale(textScale.value)
        )
    }
}

@Composable
fun PlayerArea(hands: List<List<Card>>, activeHandIndex: Int, theme: TableTheme) {
    val maxCards = hands.maxOfOrNull { it.size } ?: 1
    val scale = if (hands.size > 1) (0.85f - (maxCards - 2) * 0.05f).coerceAtLeast(0.65f) else 1f
    val handSpacing = if (maxCards > 3) (-20).dp else 16.dp

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                        ScoreIndicator(score = calculateScore(hand), alpha = scoreAlpha, theme = theme)

                        Spacer(modifier = Modifier.height(6.dp))

                        val cardSpread = 35f
                        val handWidth = 100.dp + (maxOf(0, hand.size - 1) * cardSpread).dp + 40.dp

                        Box(modifier = Modifier.width(handWidth).height(160.dp), contentAlignment = Alignment.Center) {
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
fun DealerArea(hand: List<Card>, gameState: GameState, theme: TableTheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(140.dp).fillMaxWidth()) {
            hand.forEachIndexed { index, card ->
                key(card.imageName) {
                    val targetX = (index - (hand.size - 1) / 2f) * 35f
                    val animatedX by animateFloatAsState(targetValue = targetX, animationSpec = tween(400, easing = EaseOutCubic), label = "")

                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(card.imageName) { isVisible = true }

                    val isHidden = (gameState == GameState.DEALING || gameState == GameState.ACTIVE) && index == 1
                    val flipRotation by animateFloatAsState(targetValue = if (isHidden) 180f else 0f, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "")
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
        ScoreIndicator(score = dealerVisibleScore, alpha = scoreAlpha, theme = theme)
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
fun ScoreIndicator(score: Int, alpha: Float, theme: TableTheme) {
    Surface(color = theme.brandGold, shape = RoundedCornerShape(50), modifier = Modifier.alpha(alpha)) {
        Text(text = score.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}