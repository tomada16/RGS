package com.atpp.rgs.ui.craps

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.atpp.rgs.ui.misc.CRAPS_THEME_REGISTRY
import com.atpp.rgs.ui.misc.CrapsTheme
import com.atpp.rgs.ui.shop.ItemCategory
import com.atpp.rgs.ui.shop.SHOP_CATALOG
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// ─── ZASADY GRY I STRUKTURY DANYCH ───────────────────────────────────────────

enum class CrapsPhase { COME_OUT, POINT }
enum class RoundResult { WIN, LOSE, PUSH }
enum class BetType { PASS_LINE, DONT_PASS }

data class ChipAction(val betType: BetType, val amount: Int)
data class RollHistoryItem(val total: Int)

data class CrapsUiState(
    val walletCoins: Int = 0,
    val phase: CrapsPhase = CrapsPhase.COME_OUT,
    val point: Int? = null,

    val tableBets: Map<BetType, Int> = emptyMap(),
    val lastRoundBets: Map<BetType, Int> = emptyMap(), // <--- DODANE DO REPEAT
    val chipHistory: List<ChipAction> = emptyList(),
    val selectedChipAmount: Int = 10,

    val die1: Int = 1,
    val die2: Int = 2,
    val hasRolled: Boolean = false,
    val rollHistory: List<RollHistoryItem> = emptyList(),
    val isRolling: Boolean = false,
    val roundResult: RoundResult? = null,

    val totalPayout: Int = 0,
    val totalLost: Int = 0,

    @StringRes val errorResId: Int? = null
) {
    val totalBetAmount: Int get() = tableBets.values.sum()
}

// ─── VIEWMODEL ───────────────────────────────────────────────────────────────

class CrapsViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {

    private val walletDao = app.database.walletDao()
    private val shopDao = app.database.shopDao()

    val currentTheme: StateFlow<CrapsTheme> = shopDao.getEquippedItems(userId)
        .map { equippedList ->
            val equippedTableId = equippedList.find { it.category == ItemCategory.CRAPS_TABLE.name }?.itemId
            val prefix = SHOP_CATALOG.find { it.id == equippedTableId }?.assetPrefix ?: "ocean_blue"
            CRAPS_THEME_REGISTRY[prefix] ?: CRAPS_THEME_REGISTRY["ocean_blue"]!!
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = CRAPS_THEME_REGISTRY["ocean_blue"]!!
        )

    val currentDicePrefix: StateFlow<String> = shopDao.getEquippedItems(userId)
        .map { equippedList ->
            // 1. Szukamy w bazie ubranego przedmiotu z kategorii CRAPS_DICE
            val equippedDiceId = equippedList.find { it.category == ItemCategory.CRAPS_DICE.name }?.itemId

            // 2. Szukamy jego prefiksu w katalogu (jak nie znajdzie, dajemy domyślne)
            SHOP_CATALOG.find { it.id == equippedDiceId }?.assetPrefix ?: "classic_white"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = "classic_white"
        )

    private val _state = MutableStateFlow(CrapsUiState())
    val state: StateFlow<CrapsUiState> = _state.asStateFlow()

    private val _pityGranted = MutableStateFlow(false)
    val pityGranted: StateFlow<Boolean> = _pityGranted.asStateFlow()

    fun onPityDismissed() { _pityGranted.value = false }

    init {
        viewModelScope.launch {
            walletDao.getWalletByUserId(userId).collect { wallet ->
                _state.update { it.copy(walletCoins = wallet?.coins ?: 0) }
            }
        }
    }

    fun selectChip(amount: Int) {
        _state.update { it.copy(selectedChipAmount = amount) }
    }

    // ─── MUTUALLY EXCLUSIVE BETS (ZMIANA ZAKŁADU W LOCIE) ───
    fun placeBet(betType: BetType) {
        val s = _state.value
        val amount = s.selectedChipAmount

        if (s.isRolling || s.roundResult != null) return
        if (s.phase == CrapsPhase.POINT) return // Cicho blokujemy w fazie POINT (bez błędu textowego)

        val opposingBetType = if (betType == BetType.PASS_LINE) BetType.DONT_PASS else BetType.PASS_LINE
        val opposingBetAmount = s.tableBets[opposingBetType] ?: 0

        // Czy stać nas na zakład, BAZUJĄC na tym, że kasa z przeciwnego zaraz wróci do nas?
        if (amount > (s.walletCoins + opposingBetAmount)) {
            _state.update { it.copy(errorResId = R.string.craps_error_insufficient_funds) }
            return
        }

        viewModelScope.launch {
            // 1. Zwracamy zakład przeciwstawny (jeśli istnieje)
            if (opposingBetAmount > 0) {
                walletDao.changeCoins(userId, opposingBetAmount)
            }

            // 2. Pobieramy nowy zakład
            val rowsUpdated = walletDao.deductCoins(userId, amount)
            if (rowsUpdated > 0) {
                val currentBetOnType = s.tableBets[betType] ?: 0
                val updatedBets = s.tableBets.toMutableMap().apply {
                    if (opposingBetAmount > 0) remove(opposingBetType) // Usuwamy przeciwstawny z mapy
                    put(betType, currentBetOnType + amount)
                }

                // Filtr historii: usuwamy poprzednie akcje przeciwnego zakładu, żeby "Cofnij" działało idealnie
                val filteredHistory = if (opposingBetAmount > 0) {
                    s.chipHistory.filter { it.betType != opposingBetType }
                } else s.chipHistory

                _state.update {
                    it.copy(
                        tableBets = updatedBets,
                        chipHistory = filteredHistory + ChipAction(betType, amount),
                        errorResId = null
                    )
                }
            }
        }
    }

    fun undoLastChip() {
        val s = _state.value
        if (s.chipHistory.isEmpty() || s.isRolling) return
        if (s.phase == CrapsPhase.POINT) return

        val lastAction = s.chipHistory.last()

        viewModelScope.launch {
            walletDao.changeCoins(userId, lastAction.amount)
            val currentBetOnType = s.tableBets[lastAction.betType] ?: 0
            val newAmount = (currentBetOnType - lastAction.amount).coerceAtLeast(0)

            val updatedBets = s.tableBets.toMutableMap().apply {
                if (newAmount > 0) put(lastAction.betType, newAmount)
                else remove(lastAction.betType)
            }

            _state.update {
                it.copy(
                    tableBets = updatedBets,
                    chipHistory = it.chipHistory.dropLast(1),
                    errorResId = null
                )
            }
        }
    }

    fun clearBet() {
        val s = _state.value
        if (s.isRolling || s.roundResult != null || s.totalBetAmount == 0) return

        viewModelScope.launch {
            if (s.phase == CrapsPhase.COME_OUT) {
                walletDao.changeCoins(userId, s.totalBetAmount)
                _state.update { it.copy(tableBets = emptyMap(), chipHistory = emptyList()) }
            }
        }
    }

    // ─── POWTARZANIE ZAKŁADU (REPEAT) ───
    fun repeatBet() {
        val s = _state.value
        if (s.isRolling || s.roundResult != null || s.phase == CrapsPhase.POINT) return
        if (s.lastRoundBets.isEmpty()) return

        val currentTableSum = s.totalBetAmount
        val totalNeeded = s.lastRoundBets.values.sum()

        // Uwzględniamy to, że kasa leżąca obecnie na stole do nas wróci przed postawieniem powtórki
        if (totalNeeded > (s.walletCoins + currentTableSum)) {
            _state.update { it.copy(errorResId = R.string.craps_error_insufficient_funds) }
            return
        }

        viewModelScope.launch {
            // 1. Zdejmujemy wszystko ze stołu z powrotem do portfela
            if (currentTableSum > 0) {
                walletDao.changeCoins(userId, currentTableSum)
            }

            // 2. Pobieramy kasę na powtórzony zakład
            val rowsUpdated = walletDao.deductCoins(userId, totalNeeded)
            if (rowsUpdated > 0) {
                val newHistory = s.lastRoundBets.map { ChipAction(it.key, it.value) }
                _state.update {
                    it.copy(
                        tableBets = s.lastRoundBets,
                        chipHistory = newHistory,
                        errorResId = null
                    )
                }
            }
        }
    }

    fun roll() {
        val s = _state.value
        if (s.isRolling || s.roundResult != null) return
        if (s.totalBetAmount <= 0) {
            _state.update { it.copy(errorResId = R.string.craps_error_no_bet) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isRolling = true, errorResId = null) }

            repeat(10) {
                delay(55)
                _state.update { it.copy(die1 = (1..6).random(), die2 = (1..6).random()) }
            }

            val die1 = (1..6).random()
            val die2 = (1..6).random()
            val total = die1 + die2

            processResult(die1, die2, total)
        }
    }

    private suspend fun processResult(die1: Int, die2: Int, total: Int) {
        val s = _state.value
        val newHistory = (s.rollHistory + RollHistoryItem(total)).takeLast(8)

        var rollPayout = 0
        var rollLost = 0
        var roundEnded = false
        val updatedBets = s.tableBets.toMutableMap()

        val passBet = updatedBets[BetType.PASS_LINE] ?: 0
        val dontPassBet = updatedBets[BetType.DONT_PASS] ?: 0

        when (s.phase) {
            CrapsPhase.COME_OUT -> {
                when (total) {
                    7, 11 -> { rollPayout += passBet * 2; rollLost += dontPassBet; roundEnded = true }
                    2, 3 -> { rollLost += passBet; rollPayout += dontPassBet * 2; roundEnded = true }
                    12 -> { rollLost += passBet; rollPayout += dontPassBet; roundEnded = true } // PUSH
                    else -> {
                        _state.update {
                            it.copy(
                                tableBets = updatedBets, die1 = die1, die2 = die2,
                                hasRolled = true, isRolling = false, rollHistory = newHistory,
                                phase = CrapsPhase.POINT, point = total
                            )
                        }
                    }
                }
            }
            CrapsPhase.POINT -> {
                if (total == s.point) { rollPayout += passBet * 2; rollLost += dontPassBet; roundEnded = true }
                else if (total == 7) { rollLost += passBet; rollPayout += dontPassBet * 2; roundEnded = true }
            }
        }

        if (roundEnded) {
            val savedBets = updatedBets.toMap() // ─── Zapis do REPEAT przed czyszczeniem ───
            updatedBets.clear()

            if (rollPayout > 0) {
                walletDao.changeCoins(userId, rollPayout)
            }

            if (rollLost > rollPayout && app.userRepository.checkAndGrantPity(userId)) {
                _pityGranted.value = true
            }

            val finalResult = when {
                rollPayout > (passBet + dontPassBet) -> RoundResult.WIN
                rollPayout == dontPassBet && dontPassBet > 0 && total == 12 -> RoundResult.PUSH
                else -> RoundResult.LOSE
            }
            _state.update {
                it.copy(
                    tableBets = updatedBets, lastRoundBets = savedBets, // <--- REJESTRUJEMY
                    die1 = die1, die2 = die2, hasRolled = true,
                    isRolling = false, rollHistory = newHistory,
                    roundResult = finalResult, totalPayout = rollPayout, totalLost = rollLost
                )
            }
        } else {
            _state.update {
                it.copy(
                    tableBets = updatedBets, die1 = die1, die2 = die2, hasRolled = true,
                    isRolling = false, rollHistory = newHistory
                )
            }
        }
    }

    fun newRound() {
        _state.update {
            it.copy(
                phase = CrapsPhase.COME_OUT, point = null,
                roundResult = null, totalPayout = 0, totalLost = 0, errorResId = null
            )
        }
    }

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { CrapsViewModel(app, userId) }
        }
    }
}