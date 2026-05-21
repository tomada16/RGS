package com.atpp.rgs.ui.blackjack

import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.ViewModel
import com.atpp.rgs.model.Card
import com.atpp.rgs.model.Rank
import com.atpp.rgs.model.Suit
import com.atpp.rgs.model.calculateScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.atpp.rgs.RgsApplication
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.ui.misc.THEME_REGISTRY
import com.atpp.rgs.ui.misc.TableTheme
import com.atpp.rgs.ui.shop.ItemCategory
import com.atpp.rgs.ui.shop.SHOP_CATALOG
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


enum class GameState {
    NOT_STARTED, DEALING, ACTIVE, DEALER_TURN, PLAYER_WON, DEALER_WON, TIE, PLAYER_BUSTED
}
class BlackjackViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {
    private val walletDao = app.database.walletDao()
    private val shopDao = app.database.shopDao() // Upewnij się, że masz instancję DAO

    // --- DYNAMICZNY SILNIK MOTYWÓW ---
    val currentTheme: StateFlow<TableTheme> = shopDao.getEquippedItems(userId)
        .map { equippedList ->
            // 1. Szukamy w bazie, co gracz ma ubrane na stole BJ
            val equippedTableId = equippedList.find { it.category == ItemCategory.BJ_TABLE.name }?.itemId

            // 2. Szukamy w katalogu, jaki "assetPrefix" ma ten przedmiot
            val prefix = SHOP_CATALOG.find { it.id == equippedTableId }?.assetPrefix ?: "emerald"

            // 3. Wyciągamy kompletną paletę kolorów ze słownika
            THEME_REGISTRY[prefix] ?: THEME_REGISTRY["emerald"]!!
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = THEME_REGISTRY["emerald"]!!
        )

    val currentDeckPrefix: StateFlow<String> = shopDao.getEquippedItems(userId)
        .map { equippedList ->
            // 1. Czego używamy jako kart?
            val equippedDeckId = equippedList.find { it.category == ItemCategory.BJ_DECK.name }?.itemId

            // 2. Szukamy w sklepie prefixu. Jeśli nie znajdzie (bug) - zwracamy "classic"
            SHOP_CATALOG.find { it.id == equippedDeckId }?.assetPrefix ?: "classic"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = "classic"
        )

    init {
        viewModelScope.launch {
            val wallet = walletDao.getWalletByUserId(userId).firstOrNull()
            _balance.value = (wallet?.coins ?: 0).toDouble()
        }
    }

    private val _playerHands = MutableStateFlow<List<List<Card>>>(listOf(emptyList()))
    val playerHands: StateFlow<List<List<Card>>> = _playerHands.asStateFlow()

    private val _currentHandIndex = MutableStateFlow(0)
    val currentHandIndex: StateFlow<Int> = _currentHandIndex.asStateFlow()

    private val _dealerHand = MutableStateFlow<List<Card>>(emptyList())
    val dealerHand: StateFlow<List<Card>> = _dealerHand.asStateFlow()

    private val _gameState = MutableStateFlow(GameState.NOT_STARTED)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // --- EKONOMIA (Finanse gracza) ---
    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()
    private val _currentBet = MutableStateFlow(0.0)
    val currentBet: StateFlow<Double> = _currentBet.asStateFlow()

    private var lastBetAmount = 0.0

    private var currentDeck: MutableList<Card> = mutableListOf()

    // --- LOGIKA BAZOWA ---

    private fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                val imageName = "card_${suit.prefix}${rank.suffix}"
                deck.add(Card(suit, rank, imageName))
            }
        }
        deck.shuffle()
        return deck
    }

    private fun drawCard(): Card {
        if (currentDeck.isEmpty()) currentDeck = createDeck()
        return currentDeck.removeAt(0)
    }

    // --- AKCJE GRACZA ---

    // Zarządzanie żetonami
    fun placeBet(amount: Double) {
        if (_gameState.value == GameState.NOT_STARTED || _gameState.value != GameState.ACTIVE) {
            val actualAmount = if (_balance.value >= amount) amount else _balance.value
            if (actualAmount > 0) {
                _balance.value -= actualAmount
                _currentBet.value += actualAmount
                // Zapisujemy stratę w bazie (minus)
                viewModelScope.launch { walletDao.changeCoins(userId, -actualAmount.toInt()) }
            }
        }
    }

    fun clearBet() {
        if (_gameState.value == GameState.NOT_STARTED || _gameState.value != GameState.ACTIVE) {
            val betToClear = _currentBet.value
            if (betToClear > 0) {
                _balance.value += betToClear
                _currentBet.value = 0.0
                // Zwracamy kasę do bazy (plus)
                viewModelScope.launch { walletDao.changeCoins(userId, betToClear.toInt()) }
            }
        }
    }

    fun repeatBet() {
        if ((_gameState.value == GameState.NOT_STARTED || _gameState.value != GameState.ACTIVE) && _currentBet.value == 0.0 && lastBetAmount > 0) {
            placeBet(lastBetAmount)
        }
    }

    fun hit() {
        if (_gameState.value != GameState.ACTIVE) return

        viewModelScope.launch {
            _gameState.value = GameState.DEALING

            val currentHands = _playerHands.value.toMutableList()
            val activeHandIndex = _currentHandIndex.value
            val activeHand = currentHands[activeHandIndex].toMutableList()

            activeHand.add(drawCard())
            currentHands[activeHandIndex] = activeHand
            _playerHands.value = currentHands

            val score = calculateScore(activeHand)
            if (score >= 21) {
                delay(200)
                moveToNextHandOrStand()
            } else {
                _gameState.value = GameState.ACTIVE
            }
        }
    }

    fun doubleDown() {
        if (_gameState.value != GameState.ACTIVE || _playerHands.value.size > 1 || _playerHands.value[0].size != 2) return

        viewModelScope.launch {
            _gameState.value = GameState.DEALING
            val currentBetVal = _currentBet.value
            val actualAmount = if (_balance.value >= currentBetVal) currentBetVal else _balance.value
            if (actualAmount > 0) {
                _balance.value -= actualAmount
                _currentBet.value += actualAmount
                // Pobieramy dodatkowy zakład z bazy
                viewModelScope.launch { walletDao.changeCoins(userId, -actualAmount.toInt()) }
            }

            val newHand = _playerHands.value[0].toMutableList()
            newHand.add(drawCard())
            _playerHands.value = listOf(newHand)

            val score = calculateScore(newHand)
            if (score > 21) {
                delay(200)
                performStand()
            } else {
                delay(400)
                performStand()
            }
        }
    }

    // Ta funkcja jest tylko dla kliknięcia gracza z interfejsu (UI)
    fun stand() {
        if (_gameState.value != GameState.ACTIVE) return
        // Przycisk STAY musi przejść do kolejnej ręki (jeśli jest), a nie od razu do krupiera!
        moveToNextHandOrStand()
    }


    // A to jest właściwy mózg kończący turę i podliczający wyniki
    private fun performStand() {
        viewModelScope.launch {
            _gameState.value = GameState.DEALER_TURN

            var dealerCurrentHand = _dealerHand.value.toMutableList()
            var dealerScore = calculateScore(dealerCurrentHand)

            val anyHandNotBusted = _playerHands.value.any { calculateScore(it) <= 21 }

            if (anyHandNotBusted) {
                delay(400)

                while (dealerScore < 17) {
                    dealerCurrentHand.add(drawCard())
                    _dealerHand.value = dealerCurrentHand.toList()
                    dealerScore = calculateScore(dealerCurrentHand)
                    delay(500)
                }

                delay(300)
            } else {
                delay(150)
            }

            var totalPayoutMultiplier = 0.0
            var allBusted = true

            for (hand in _playerHands.value) {
                val playerScore = calculateScore(hand)
                val handBetFraction = 1.0 / _playerHands.value.size

                if (playerScore <= 21) {
                    allBusted = false
                    if (dealerScore > 21 || playerScore > dealerScore) {
                        totalPayoutMultiplier += (2.0 * handBetFraction)
                    } else if (playerScore == dealerScore) {
                        totalPayoutMultiplier += (1.0 * handBetFraction)
                    }
                }
            }

            if (allBusted) {
                _gameState.value = GameState.PLAYER_BUSTED
            } else if (dealerScore > 21 || totalPayoutMultiplier > 1.0) {
                _gameState.value = GameState.PLAYER_WON
            } else if (totalPayoutMultiplier == 1.0) {
                _gameState.value = GameState.TIE
            } else {
                _gameState.value = GameState.DEALER_WON
            }

            payout(totalPayoutMultiplier)
        }
    }

    fun split() {
        if (_gameState.value != GameState.ACTIVE) return

        val currentHands = _playerHands.value
        if (currentHands.size != 1) return
        val firstHand = currentHands[0]
        if (firstHand.size != 2 || firstHand[0].rank != firstHand[1].rank) return

        val splitBetAmount = _currentBet.value
        if (_balance.value >= splitBetAmount) {
            _balance.value -= splitBetAmount
            _currentBet.value += splitBetAmount
            // Pobieramy rozbity zakład z bazy
            viewModelScope.launch { walletDao.changeCoins(userId, -splitBetAmount.toInt()) }
        } else {
            return
        }

        viewModelScope.launch {
            _gameState.value = GameState.DEALING

            // Faza 1: Rozdzielamy pierwszą rękę na dwie pojedyncze karty
            val hand1 = mutableListOf(firstHand[0])
            val hand2 = mutableListOf(firstHand[1])
            _playerHands.value = listOf(hand1, hand2)
            _currentHandIndex.value = 0

            delay(400) // Czekamy na rozsunięcie

            // Faza 2: Dobieramy drugą kartę TYLKO do pierwszej ręki
            hand1.add(drawCard())
            _playerHands.value = listOf(hand1.toList(), hand2.toList())

            delay(400)

            // Faza 3: Przekazujemy pałeczkę
            if (calculateScore(hand1) == 21) {
                moveToNextHandOrStand()
            } else {
                _gameState.value = GameState.ACTIVE
            }
        }
    }

    private fun moveToNextHandOrStand() {
        if (_currentHandIndex.value < _playerHands.value.size - 1) {
            _currentHandIndex.value += 1

            // NOWOŚĆ: Kiedy przechodzimy do drugiej ręki, dobieramy jej brakującą kartę!
            viewModelScope.launch {
                _gameState.value = GameState.DEALING // Znowu blokujemy na czas animacji

                delay(300) // Dajemy ułamek sekundy na "podświetlenie" nowej ręki w UI

                val currentHands = _playerHands.value.toMutableList()
                val activeHandIndex = _currentHandIndex.value
                val activeHand = currentHands[activeHandIndex].toMutableList()

                activeHand.add(drawCard())
                currentHands[activeHandIndex] = activeHand
                _playerHands.value = currentHands

                delay(400) // Czekamy aż karta wleci na stół

                if (calculateScore(activeHand) == 21) {
                    moveToNextHandOrStand()
                } else {
                    _gameState.value = GameState.ACTIVE
                }
            }
        } else {
            // Rozegraliśmy wszystkie ręce
            performStand()
        }
    }
    fun startNewGame() {
        if (_currentBet.value == 0.0) return
        lastBetAmount = _currentBet.value

        viewModelScope.launch {
            _gameState.value = GameState.DEALING
            currentDeck = createDeck()

            // Resetujemy ręce na jedną pustą
            _playerHands.value = listOf(emptyList())
            _currentHandIndex.value = 0
            _dealerHand.value = emptyList()

            delay(300)
            _playerHands.value = listOf(listOf(drawCard())) // Pierwsza karta do pierwszej ręki
            delay(300)
            _dealerHand.value = listOf(drawCard())
            delay(300)
            // Dodajemy drugą kartę do pierwszej ręki
            val currentHand = _playerHands.value[0].toMutableList()
            currentHand.add(drawCard())
            _playerHands.value = listOf(currentHand)
            delay(300)
            _dealerHand.value = _dealerHand.value + drawCard()

            _gameState.value = GameState.ACTIVE

            // Sprawdzenie Blackjacka
            if (calculateScore(_playerHands.value[0]) == 21) {
                delay(600)
                _gameState.value = GameState.PLAYER_WON
                payout(multiplier = 2.5)
            }
        }
    }

    private fun payout(multiplier: Double) {
        val wonAmount = _currentBet.value * multiplier
        _balance.value += wonAmount
        _currentBet.value = 0.0

        if (wonAmount > 0) {
            // Wygrywamy (lub mamy remis), wpłacamy profit do bazy
            viewModelScope.launch { walletDao.changeCoins(userId, wonAmount.toInt()) }
        }
    }

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { BlackjackViewModel(app, userId) }
        }
    }
}