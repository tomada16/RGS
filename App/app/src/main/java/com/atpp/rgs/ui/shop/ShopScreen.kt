package com.atpp.rgs.ui.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.blackjack.formatFullCurrency
import com.atpp.rgs.ui.misc.THEME_REGISTRY
import com.atpp.rgs.ui.misc.CRAPS_THEME_REGISTRY
import com.atpp.rgs.ui.misc.TableTheme
import com.atpp.rgs.ui.theme.BrandBlack
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandRed
import com.atpp.rgs.ui.theme.BrandRedDark
import java.util.Locale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.zIndex

val ShopSurface = Color(0xFF1E1A1A)
val ShopGold = Color(0xFFD4AF37)
val ShopGoldMuted = Color(0xFF9E7E38)
val ShopTextLight = Color(0xFFE0E0E0)
val ShopTextMuted = Color(0xFF888888)

@Composable
fun ShopScreen(
    app: RgsApplication,
    userId: Int,
    modifier: Modifier = Modifier,
    viewModel: ShopViewModel = viewModel(factory = ShopViewModel.factory(app, userId))
) {
    val balance by viewModel.balance.collectAsState()
    val ownedItems by viewModel.ownedItems.collectAsState()
    val equippedItems by viewModel.equippedItems.collectAsState()

    var selectedCategory by remember { mutableStateOf(ItemCategory.BJ_TABLE) }
    var previewItem by remember { mutableStateOf<ShopItem?>(null) }

    val bgGradient = Brush.verticalGradient(
        0.00f to BrandBlack,
        0.28f to BrandRedDark,
        0.50f to BrandRed,
        0.72f to BrandRedDark,
        1.00f to BrandBlack
    )

    // GŁÓWNY KONTENER EKRANU
    Box(modifier = modifier.fillMaxSize()) {

        // STANDARDOWA ZAWARTOŚĆ SKLEPU
        Column(modifier = Modifier.fillMaxSize()) {
            ShopTopBar(balance = balance)

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BrandGold))

            Box(modifier = Modifier.fillMaxWidth().background(BrandBlack)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryPill("BJ TABLES", selectedCategory == ItemCategory.BJ_TABLE) { selectedCategory = ItemCategory.BJ_TABLE }
                    Spacer(Modifier.width(8.dp))
                    CategoryPill("DECKS", selectedCategory == ItemCategory.BJ_DECK) { selectedCategory = ItemCategory.BJ_DECK }
                    Spacer(Modifier.width(8.dp))
                    CategoryPill("CRAPS", selectedCategory == ItemCategory.CRAPS_TABLE) { selectedCategory = ItemCategory.CRAPS_TABLE }
                    Spacer(Modifier.width(8.dp))
                    CategoryPill("DICE", selectedCategory == ItemCategory.CRAPS_DICE) { selectedCategory = ItemCategory.CRAPS_DICE }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(bgGradient)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    val displayItems = SHOP_CATALOG.filter { it.category == selectedCategory }
                    items(displayItems) { item ->
                        val isOwned = ownedItems.contains(item.id)
                        val isEquipped = equippedItems[item.category.name] == item.id
                        val canAfford = balance >= item.price

                        ShopItemCard(
                            item = item,
                            isOwned = isOwned,
                            isEquipped = isEquipped,
                            canAfford = canAfford,
                            onBuyClick = { viewModel.buyItem(item) },
                            onEquipClick = { viewModel.equipItem(item) },
                            onCardClick = {
                                if (item.category == ItemCategory.BJ_TABLE || item.category == ItemCategory.CRAPS_TABLE) {
                                    previewItem = item
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // --- NASZ NOWY, WEWNĘTRZNY MODAL (OVERLAY) ---
        // Zapewnia płynne animacje i nie psuje paska statusu!
        AnimatedVisibility(
            visible = previewItem != null,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.zIndex(10f)
        ) {
            previewItem?.let { item ->
                ThemePreviewOverlay(
                    item = item,
                    onDismiss = { previewItem = null }
                )
            }
        }
    }
}

@Composable
fun ThemePreviewOverlay(item: ShopItem, onDismiss: () -> Unit) {
    val isCraps = item.category == ItemCategory.CRAPS_TABLE

    // --- DYNAMICZNE WYCIĄGANIE DANYCH ---
    val bgResId: Int
    val goldColor: Color
    val goldLightColor: Color
    val buttonDarkColor: Color
    val buttonStayColor: Color? // Tylko w BJ
    val btn1Text: String
    val btn2Text: String?

    if (isCraps) {
        val theme = CRAPS_THEME_REGISTRY[item.assetPrefix] ?: CRAPS_THEME_REGISTRY["ocean_blue"]!!
        bgResId = theme.bgResId
        goldColor = theme.brandGold
        goldLightColor = theme.brandGoldLight
        buttonDarkColor = theme.buttonDark
        buttonStayColor = null // Craps nie ma przycisku STAND
        btn1Text = "ROLL"
        btn2Text = null
    } else {
        val theme = THEME_REGISTRY[item.assetPrefix] ?: THEME_REGISTRY["emerald"]!!
        bgResId = theme.bgResId
        goldColor = theme.brandGold
        goldLightColor = theme.brandGoldLight
        buttonDarkColor = theme.buttonDark
        buttonStayColor = theme.buttonStay
        btn1Text = "HIT"
        btn2Text = "STAND"
    }

    // Zapobiega falowaniu (ripple effect) przy klikaniu w puste tło
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)) // Tło zaciemniające
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Brak animacji kliknięcia w tło
                onClick = onDismiss // Kliknięcie w tło zamyka modal
            ),
        contentAlignment = Alignment.Center
    ) {
        // Kontener samego modala
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                // Ten modyfikator "kradnie" kliknięcia, żeby kliknięcie w sam modal go nie zamykało
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(24.dp),
            color = BrandBlack,
            border = BorderStroke(1.dp, goldColor.copy(alpha = 0.5f)) // Ramka pod kolor wybranego motywu!
        ) {
            Column {
                Image(
                    painter = painterResource(id = bgResId),
                    contentDescription = "Preview Background",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = item.name, color = ShopTextLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = item.description, color = ShopTextMuted, fontSize = 12.sp, letterSpacing = 2.sp)

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = BrandGold.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("THEME PALETTE", color = BrandGold, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ColorSwatch(goldColor)
                        ColorSwatch(goldLightColor)
                        ColorSwatch(buttonDarkColor)
                        if (buttonStayColor != null) {
                            ColorSwatch(buttonStayColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text("CONTROLS PREVIEW", color = BrandGold, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCraps) {
                            // Dla Craps pokazujemy jeden duży złoty przycisk "ROLL"
                            PreviewButton(btn1Text, goldColor, Color(0xFF1A0F00), Modifier.weight(1f))
                        } else {
                            // Dla BJ pokazujemy dwa przyciski "HIT" i "STAND"
                            PreviewButton(btn1Text, Color.White, Color.Black, Modifier.weight(1f))
                            PreviewButton(btn2Text!!, buttonStayColor!!, goldColor, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    )
}

@Composable
fun PreviewButton(text: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Komponenty widoku sklepu (Pill, Card, TopBar)
// ─────────────────────────────────────────────────────────────
@Composable
fun CategoryPill(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) BrandGold else ShopTextMuted.copy(alpha = 0.3f)

    Surface(
        color = if (isSelected) BrandGold else Color.Transparent,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else ShopTextLight.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun ShopTopBar(balance: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlack)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))

        Icon(
            imageVector        = Icons.Filled.MonetizationOn,
            contentDescription = "Coins",
            tint               = BrandGold,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text         = String.format(Locale.US, "%,d", balance),
            color        = BrandGold,
            fontSize     = 18.sp,
            fontWeight   = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onBuyClick: () -> Unit,
    onEquipClick: () -> Unit,
    onCardClick: () -> Unit, // Zdarzenie kliknięcia w kartę
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }, // Cała karta reaguje na kliknięcie!
        colors = CardDefaults.cardColors(containerColor = ShopSurface),
    ) {
        Column {
            Image(
                painter = painterResource(id = item.storeImageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = item.name, color = ShopTextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = item.description, color = ShopTextMuted, fontSize = 11.sp, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))

                if (isEquipped) {
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                        enabled = false
                    ) {
                        Text("EQUIPPED", color = ShopGoldMuted, fontWeight = FontWeight.Bold)
                    }
                } else if (isOwned) {
                    Button(
                        onClick = onEquipClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        border = BorderStroke(1.dp, BrandGold)
                    ) {
                        Text("EQUIP", color = BrandGold, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onBuyClick,
                        enabled = canAfford,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGold,
                            contentColor = Color.Black,
                            disabledContainerColor = ShopGoldMuted.copy(alpha = 0.3f),
                            disabledContentColor = Color.Black.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("$${formatFullCurrency(item.price.toDouble())}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}