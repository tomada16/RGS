package com.atpp.rgs.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.entity.EquippedItemEntity
import com.atpp.rgs.data.entity.OwnedItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ItemCategory { BJ_TABLE, BJ_DECK, CRAPS_TABLE, CRAPS_DICE }

data class ShopItem(
    val id: String,
    val category: ItemCategory,
    val name: String,
    val description: String,
    val price: Int,
    val assetPrefix: String,
    val storeImageResId: Int
)

// Globalny katalog produktów z ZESTAWEM STARTOWYM
val SHOP_CATALOG = listOf(
    // STOŁY
    ShopItem("bj_table_emerald", ItemCategory.BJ_TABLE, "Emerald Felt", "CLASSIC CASINO", 50_000, "emerald", R.drawable.bj_table_bg),
    ShopItem("bj_table_burgundy", ItemCategory.BJ_TABLE, "Royal Burgundy", "HIGH STAKES", 10, "burgundy", R.drawable.burgundy_bj_table_bg),
    ShopItem("bj_table_midnight", ItemCategory.BJ_TABLE, "Midnight Velvet", "PRIVATE LOUNGE", 250, "midnight", R.drawable.midnight_bj_table_bg),

    // KARTY (Domyślna talia dodana do katalogu, w cenie 0, bo i tak dajesz to za darmo)
    ShopItem("bj_deck_classic", ItemCategory.BJ_DECK, "Classic Standard", "TRADITIONAL DECK", 0, "classic", R.drawable.card_spadesace) // Zmień R.drawable na podgląd rewersu!
)

class ShopViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {

    private val shopDao = app.database.shopDao()
    private val walletDao = app.database.walletDao()

    init {
        // --- ZESTAW STARTOWY (STARTER PACK) ---
        viewModelScope.launch {
            // 1. Zapewniamy, że gracz ZAWSZE posiada podstawowe przedmioty (Dao zignoruje, jeśli już ma)
            shopDao.insertOwnedItem(OwnedItemEntity(userId, "bj_table_emerald"))
            shopDao.insertOwnedItem(OwnedItemEntity(userId, "bj_deck_classic"))

            // 2. Jeśli gracz to "świeżak" i nie ma wyekwipowanego NIC, zakładamy mu domyślne itemy
            val currentEquip = shopDao.getEquippedItems(userId).firstOrNull()
            if (currentEquip.isNullOrEmpty()) {
                shopDao.equipItem(EquippedItemEntity(userId, ItemCategory.BJ_TABLE.name, "bj_table_emerald"))
                shopDao.equipItem(EquippedItemEntity(userId, ItemCategory.BJ_DECK.name, "bj_deck_classic"))
            }
        }
    }

    val balance: StateFlow<Int> = walletDao.getWalletByUserId(userId)
        .combine(MutableStateFlow(0)) { wallet, _ -> wallet?.coins ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val ownedItems: StateFlow<Set<String>> = shopDao.getOwnedItems(userId)
        .combine(MutableStateFlow(emptySet<String>())) { list, _ -> list.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val equippedItems: StateFlow<Map<String, String>> = shopDao.getEquippedItems(userId)
        .combine(MutableStateFlow(emptyMap<String, String>())) { list, _ ->
            list.associate { it.category to it.itemId }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun buyItem(item: ShopItem) {
        viewModelScope.launch {
            if (balance.value >= item.price && !ownedItems.value.contains(item.id)) {
                val success = shopDao.buyItemTransaction(userId, item.id, item.price)
                if (success) {
                    equipItem(item)
                }
            }
        }
    }

    fun equipItem(item: ShopItem) {
        viewModelScope.launch {
            if (ownedItems.value.contains(item.id)) {
                shopDao.equipItem(EquippedItemEntity(userId, item.category.name, item.id))
            }
        }
    }

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { ShopViewModel(app, userId) }
        }
    }
}