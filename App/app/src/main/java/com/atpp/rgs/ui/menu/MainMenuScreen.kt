package com.atpp.rgs.ui.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.entity.GameEntity
import com.atpp.rgs.data.entity.WalletEntity
import com.atpp.rgs.ui.components.GradientButton
import com.atpp.rgs.ui.profile.ProfileScreen
import com.atpp.rgs.ui.settings.SettingsScreen
import com.atpp.rgs.ui.shop.ShopScreen
import com.atpp.rgs.ui.theme.BrandBlack
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandGoldDark
import com.atpp.rgs.ui.theme.BrandPanel
import com.atpp.rgs.ui.theme.BrandRed
import com.atpp.rgs.ui.theme.BrandRedDark
import com.atpp.rgs.ui.theme.BrandTextMuted
import com.atpp.rgs.ui.theme.BrandWhite
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// Zakładki dolnej nawigacji.
// Aby dodać nowy ekran: dopisz wartość tu + utwórz nowy plik ekranu
// + dodaj gałąź w when (selectedTab) w MainMenuScreen.
// ─────────────────────────────────────────────────────────────

enum class MainTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    MENU(R.string.nav_menu, Icons.Filled.Home),
    SHOP(R.string.nav_shop, Icons.Filled.ShoppingCart),
    SETTINGS(R.string.nav_settings, Icons.Filled.Settings),
    PROFILE(R.string.nav_profile, Icons.Filled.Person)
}

// ─────────────────────────────────────────────────────────────
// Główny composable ekranu
// ─────────────────────────────────────────────────────────────

@Composable
fun MainMenuScreen(
    app: RgsApplication,
    userId: Int,
    viewModel: MainMenuViewModel = viewModel(factory = MainMenuViewModel.factory(app, userId)),
    onNavigateToBlackjack: () -> Unit
) {
    val games  by viewModel.games.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    var selectedTab by remember { mutableStateOf(MainTab.MENU) }

    Scaffold(
        containerColor = BrandBlack,
        bottomBar = {
            MainBottomNavBar(
                selectedTab   = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.MENU -> MenuContent(
                games      = games,
                wallet     = wallet,
                onJoinGame = { game ->
                    when (game.name.lowercase(Locale.ROOT)) {
                        "blackjack" -> onNavigateToBlackjack()
                        // "craps" -> onNavigateToCraps() // Miejsce na kolejne gry
                    }
                },
                modifier   = Modifier.padding(innerPadding)
            )
            // PRZYWRÓCONE ZAKŁADKI KOLEGI:
            MainTab.SHOP -> ShopScreen(
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.PROFILE -> ProfileScreen(
                onLogout = viewModel::logout,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Zawartość zakładki MENU — lista gier
// ─────────────────────────────────────────────────────────────

@Composable
private fun MenuContent(
    games: List<GameEntity>,
    wallet: WalletEntity?,
    onJoinGame: (GameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Gradient tła: czarne krawędzie (więcej czerninego od góry i dołu)
    // rozjaśniające się ku środkowi gdzie dominuje czerwień.
    val bgGradient = Brush.verticalGradient(
        0.00f to BrandBlack,
        0.28f to BrandRedDark,
        0.50f to BrandRed,
        0.72f to BrandRedDark,
        1.00f to BrandBlack
    )

    Column(modifier = modifier.fillMaxSize()) {

        // ── Górny pasek (portfel) ──────────────────────────────
        MainTopBar(wallet = wallet)

        // ── Złota linia oddzielająca pasek od treści ──────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BrandGold)
        )

        // ── Obszar z grami: gradient tła + przewijalna lista ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(bgGradient)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { VipLoungeHeader() }

                items(items = games, key = { it.id }) { game ->
                    GameTile(
                        game   = game,
                        onJoin = { onJoinGame(game) }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Górny pasek
// ─────────────────────────────────────────────────────────────

@Composable
private fun MainTopBar(wallet: WalletEntity?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlack)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lewa strona — puste miejsce na przyszłe elementy (np. avatar)
        Spacer(Modifier.weight(1f))

        // Prawa strona — saldo portfela z bazy danych
        Icon(
            imageVector        = Icons.Filled.MonetizationOn,
            contentDescription = stringResource(R.string.menu_cd_coins),
            tint               = BrandGold,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text         = formatCoins(wallet?.coins),
            color        = BrandGold,
            fontSize     = 18.sp,
            fontWeight   = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private fun formatCoins(coins: Int?): String =
    if (coins == null) "—"
    else String.format(Locale.US, "%,d", coins)

// ─────────────────────────────────────────────────────────────
// Nagłówek VIP Lounge
// ─────────────────────────────────────────────────────────────

@Composable
private fun VipLoungeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text          = stringResource(R.string.menu_welcome_to_the),
            color         = BrandGold,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = stringResource(R.string.menu_vip_lounge),
            color      = BrandWhite,
            fontSize   = 38.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(BrandGold, BrandGoldDark)))
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Kafelek gry
// ─────────────────────────────────────────────────────────────

@Composable
private fun GameTile(
    game: GameEntity,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape    = RoundedCornerShape(16.dp)
    val imageRes = gameImageRes(game.name)

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(240.dp)
            .clip(shape)
            .clickable { onJoin() }  // kliknięcie w dowolne miejsce kafelka
    ) {
        // ── Zdjęcie gry jako tło ──
        if (imageRes != null) {
            Image(
                painter            = painterResource(imageRes),
                contentDescription = stringResource(R.string.menu_cd_game_image, game.name),
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            // Fallback gdy brak zdjęcia dla tej gry
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(BrandRedDark, BrandPanel))
                    )
            )
        }

        // ── Gradient nakładka: przezroczysta u góry → ciemna u dołu ──
        // Sprawia że tekst i przycisk na dole są zawsze czytelne.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.00f to Color.Transparent,
                        0.45f to Color(0x55000000),
                        1.00f to Color(0xD9000000)
                    )
                )
        )

        // ── Tekst + przycisk JOIN w dolnej części kafelka ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text       = game.name,
                color      = BrandWhite,
                fontSize   = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = game.description,
                color      = BrandTextMuted,
                fontSize   = 13.sp,
                maxLines   = 2,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))
            GradientButton(
                text    = stringResource(R.string.menu_btn_join),
                onClick = onJoin
            )
        }
    }
}

/**
 * Mapowanie nazwa gry → zasób drawable.
 * Aby dodać kolejną grę: dopisz wpis i wrzuć plik PNG do res/drawable/.
 */
@DrawableRes
private fun gameImageRes(name: String): Int? = when (name.lowercase(Locale.ROOT)) {
    "blackjack" -> R.drawable.bg_blackjack
    "craps"     -> R.drawable.bg_craps
    else        -> null
}

// ─────────────────────────────────────────────────────────────
// Dolna nawigacja — tylko ikony, bez etykiet
// ─────────────────────────────────────────────────────────────

@Composable
private fun MainBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = BrandPanel,
        tonalElevation = 0.dp
    ) {
        MainTab.values().forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick  = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector        = tab.icon,
                        contentDescription = stringResource(tab.labelRes)
                    )
                },
                label = null,          // brak etykiet — tylko ikony
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = BrandGold,
                    indicatorColor      = BrandRedDark,
                    unselectedIconColor = BrandTextMuted
                )
            )
        }
    }
}
