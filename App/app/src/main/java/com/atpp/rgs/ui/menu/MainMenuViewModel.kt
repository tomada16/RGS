package com.atpp.rgs.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.entity.GameEntity
import com.atpp.rgs.data.entity.WalletEntity
import com.atpp.rgs.data.repository.UserRepository
import com.atpp.rgs.ui.shop.ItemCategory // <--- DODANY IMPORT
import com.atpp.rgs.data.entity.EquippedItemEntity // <--- DODANY IMPORT
import com.atpp.rgs.data.entity.OwnedItemEntity // <--- DODANY IMPORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainMenuViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {

    val games: StateFlow<List<GameEntity>> = app.database.gameDao()
        .getAllGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val wallet: StateFlow<WalletEntity?> = app.database.walletDao()
        .getWalletByUserId(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _pityGranted = MutableStateFlow(false)
    val pityGranted: StateFlow<Boolean> = _pityGranted.asStateFlow()

    // --- NOWOŚĆ: INICJALIZACJA KONTA (STARTER PACK) ---
    init {
        viewModelScope.launch {
            val shopDao = app.database.shopDao()

            // 1. Zapewniamy, że gracz ZAWSZE posiada podstawowe przedmioty
            // (Jeśli ma w DAO ustawione @Insert(onConflict = OnConflictStrategy.IGNORE), to nic się nie zepsuje przy kolejnych logowaniach)
            shopDao.insertOwnedItem(OwnedItemEntity(userId, "bj_table_emerald"))
            shopDao.insertOwnedItem(OwnedItemEntity(userId, "bj_deck_classic"))
            shopDao.insertOwnedItem(OwnedItemEntity(userId, "craps_table_ocean"))

            // 2. Jeśli gracz to "świeżak" i nie ma wyekwipowanego NIC, zakładamy mu domyślne itemy
            val currentEquip = shopDao.getEquippedItems(userId).firstOrNull()
            if (currentEquip.isNullOrEmpty()) {
                shopDao.equipItem(EquippedItemEntity(userId, ItemCategory.BJ_TABLE.name, "bj_table_emerald"))
                shopDao.equipItem(EquippedItemEntity(userId, ItemCategory.BJ_DECK.name, "bj_deck_classic"))
                shopDao.equipItem(EquippedItemEntity(userId, ItemCategory.CRAPS_TABLE.name, "craps_table_ocean"))
            }
        }
    }

    fun checkPity() {
        viewModelScope.launch {
            val currentWallet = app.database.walletDao().getWalletByUserId(userId).first()
            if (currentWallet != null && currentWallet.coins < UserRepository.PITY_THRESHOLD) {
                val granted = app.userRepository.checkAndGrantPity(userId)
                if (granted) {
                    _pityGranted.value = true
                }
            }
        }
    }

    fun onPityDismissed() {
        _pityGranted.value = false
    }

    fun logout() {
        viewModelScope.launch { app.sessionManager.logout() }
    }

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainMenuViewModel(app, userId) }
        }
    }
}