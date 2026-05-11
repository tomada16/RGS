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

// ─── Fazy gry ────────────────────────────────────────────────────────────────

/** Faza rzutu wyjściowego (come-out) lub faza punktu. */
enum class CrapsPhase { COME_OUT, POINT }

/** Wynik zamkniętej rundy. */
enum class RoundResult { WIN, LOSE }

// ─── Stan UI ─────────────────────────────────────────────────────────────────

/**
 * Wpis w historii rzutów widoczny jako mały kafelek.
 */
data class RollHistoryItem(val total: Int)

/**
 * Kompletny, niemutowalny stan ekranu Craps.
 * Emitowany przez [CrapsViewModel.state] jako StateFlow.
 */
data class CrapsUiState(
    /** Saldo portfela gracza (aktualizowane reaktywnie z Room). */
    val walletCoins: Int = 0,

    /** Aktualna faza gry. */
    val phase: CrapsPhase = CrapsPhase.COME_OUT,

    /** Ustalona liczba punktu (null w fazie COME_OUT). */
    val point: Int? = null,

    /** Łączna wartość postawionych żetonów w tej rundzie. */
    val currentBet: Int = 0,

    /**
     * Historia dodanych żetonów (wartości) — służy do obsługi „Cofnij".
     * Zablokowana w fazie POINT (zakład jest wtedy zamrożony).
     */
    val chipHistory: List<Int> = emptyList(),

    /** Wartość pierwszej kostki (1–6). */
    val die1: Int = 1,

    /** Wartość drugiej kostki (1–6). */
    val die2: Int = 2,

    /** True, gdy pierwsza kostka została kiedykolwiek rzucona w tej rundzie. */
    val hasRolled: Boolean = false,

    /** Ostatnie maks. 8 wyrzuconych sum — do wyświetlenia w historii. */
    val rollHistory: List<RollHistoryItem> = emptyList(),

    /** True podczas animacji toczenia kostek. */
    val isRolling: Boolean = false,

    /** Wynik rundy lub null gdy gra trwa. */
    val roundResult: RoundResult? = null,

    /** Kwota wygranej/przegranej — wyświetlana na nakładce wyniku. */
    val lastBetAmount: Int = 0,

    /** Identyfikator zasobu błędu do wyświetlenia (null = brak błędu). */
    @StringRes val errorResId: Int? = null
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class CrapsViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {

    private val walletDao = app.database.walletDao()

    private val _state = MutableStateFlow(CrapsUiState())
    val state: StateFlow<CrapsUiState> = _state.asStateFlow()

    init {
        // Obserwuj portfel reaktywnie — Room emituje nowe wartości po każdej zmianie.
        viewModelScope.launch {
            walletDao.getWalletByUserId(userId).collect { wallet ->
                _state.update { it.copy(walletCoins = wallet?.coins ?: 0) }
            }
        }
    }

    // ─── Obsługa zakładu ─────────────────────────────────────────────────────

    /**
     * Dodaje żeton o wartości [amount] do bieżącego zakładu.
     * Dostępne tylko w fazie COME_OUT (zakład jest zamrożony po ustaleniu punktu).
     */
    fun addChip(amount: Int) {
        val s = _state.value
        if (s.phase == CrapsPhase.POINT || s.isRolling || s.roundResult != null) return

        val newBet = s.currentBet + amount
        if (newBet > s.walletCoins) {
            _state.update { it.copy(errorResId = R.string.craps_error_insufficient_funds) }
            return
        }
        _state.update {
            it.copy(
                currentBet  = newBet,
                chipHistory = it.chipHistory + amount,
                errorResId  = null
            )
        }
    }

    /**
     * Cofa ostatnio dodany żeton.
     * Dostępne tylko w fazie COME_OUT.
     */
    fun undoLastChip() {
        val s = _state.value
        if (s.phase == CrapsPhase.POINT || s.chipHistory.isEmpty() || s.isRolling) return
        val last = s.chipHistory.last()
        _state.update {
            it.copy(
                currentBet  = it.currentBet - last,
                chipHistory = it.chipHistory.dropLast(1),
                errorResId  = null
            )
        }
    }

    // ─── Rzut kostkami ───────────────────────────────────────────────────────

    /**
     * Rzuca kostkami:
     * 1. Krótka animacja (~600 ms) z losowymi wartościami.
     * 2. Finalny wynik przekazywany do [processResult].
     */
    fun roll() {
        val s = _state.value
        if (s.isRolling || s.roundResult != null) return
        if (s.currentBet <= 0) {
            _state.update { it.copy(errorResId = R.string.craps_error_no_bet) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isRolling = true, errorResId = null) }

            // Animacja toczenia — szybkie losowe zmiany
            repeat(10) {
                delay(55)
                _state.update { it.copy(die1 = (1..6).random(), die2 = (1..6).random()) }
            }

            // Finalny wyrzut
            val die1  = (1..6).random()
            val die2  = (1..6).random()
            val total = die1 + die2

            processResult(die1, die2, total)
        }
    }

    /**
     * Przetwarza wynik rzutu zgodnie z zasadami Craps (Pass Line):
     *
     * Faza COME_OUT:
     *  - 7 lub 11  → WIN (Natural)
     *  - 2, 3, 12  → LOSE (Craps)
     *  - inne      → ustalenie punktu, przejście do fazy POINT
     *
     * Faza POINT:
     *  - suma == punkt → WIN
     *  - 7             → LOSE (Seven-out)
     *  - inne          → kontynuuj rzuty
     */
    private suspend fun processResult(die1: Int, die2: Int, total: Int) {
        val s = _state.value
        val newHistory = (s.rollHistory + RollHistoryItem(total)).takeLast(8)

        when (s.phase) {

            CrapsPhase.COME_OUT -> when (total) {
                7, 11 -> {
                    // Natural — Pass Line wygrywa
                    walletDao.changeCoins(userId, s.currentBet)
                    _state.update {
                        it.copy(
                            die1 = die1, die2 = die2, hasRolled = true,
                            isRolling = false, rollHistory = newHistory,
                            roundResult = RoundResult.WIN, lastBetAmount = s.currentBet
                        )
                    }
                }
                2, 3, 12 -> {
                    // Craps — Pass Line przegrywa
                    walletDao.changeCoins(userId, -s.currentBet)
                    _state.update {
                        it.copy(
                            die1 = die1, die2 = die2, hasRolled = true,
                            isRolling = false, rollHistory = newHistory,
                            roundResult = RoundResult.LOSE, lastBetAmount = s.currentBet
                        )
                    }
                }
                else -> {
                    // Ustalenie punktu — gra trwa
                    _state.update {
                        it.copy(
                            die1 = die1, die2 = die2, hasRolled = true,
                            isRolling = false, rollHistory = newHistory,
                            phase = CrapsPhase.POINT, point = total
                        )
                    }
                }
            }

            CrapsPhase.POINT -> when (total) {
                s.point -> {
                    // Trafił punkt — Pass Line wygrywa
                    walletDao.changeCoins(userId, s.currentBet)
                    _state.update {
                        it.copy(
                            die1 = die1, die2 = die2,
                            isRolling = false, rollHistory = newHistory,
                            roundResult = RoundResult.WIN, lastBetAmount = s.currentBet
                        )
                    }
                }
                7 -> {
                    // Seven-out — Pass Line przegrywa
                    walletDao.changeCoins(userId, -s.currentBet)
                    _state.update {
                        it.copy(
                            die1 = die1, die2 = die2,
                            isRolling = false, rollHistory = newHistory,
                            roundResult = RoundResult.LOSE, lastBetAmount = s.currentBet
                        )
                    }
                }
                else -> {
                    // Ani punkt ani siódemka — kontynuuj
                    _state.update {
                        it.copy(
                            die1 = die1, die2 = die2,
                            isRolling = false, rollHistory = newHistory
                        )
                    }
                }
            }
        }
    }

    // ─── Reset rundy ─────────────────────────────────────────────────────────

    /**
     * Resetuje stan do nowej rundy po wyświetleniu wyniku.
     * Historia rzutów pozostaje nienaruszona.
     */
    fun newRound() {
        _state.update {
            it.copy(
                phase        = CrapsPhase.COME_OUT,
                point        = null,
                currentBet   = 0,
                chipHistory  = emptyList(),
                hasRolled    = false,
                roundResult  = null,
                lastBetAmount = 0,
                errorResId    = null
            )
        }
    }

    // ─── Factory ─────────────────────────────────────────────────────────────

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { CrapsViewModel(app, userId) }
        }
    }
}
