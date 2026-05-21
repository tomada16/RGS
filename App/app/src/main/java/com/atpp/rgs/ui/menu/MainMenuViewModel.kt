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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainMenuViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {

    /** Lista wszystkich gier z bazy danych — reaktywna. */
    val games: StateFlow<List<GameEntity>> = app.database.gameDao()
        .getAllGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Portfel zalogowanego użytkownika — reaktywny. */
    val wallet: StateFlow<WalletEntity?> = app.database.walletDao()
        .getWalletByUserId(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _pityGranted = MutableStateFlow(false)
    val pityGranted: StateFlow<Boolean> = _pityGranted.asStateFlow()

    private var isPityProcessing = false

    init {
        viewModelScope.launch {
            wallet.filterNotNull().collect { w ->
                if (w.coins < UserRepository.PITY_THRESHOLD && !isPityProcessing) {
                    isPityProcessing = true
                    val granted = app.userRepository.checkAndGrantPity(userId)
                    if (granted) _pityGranted.value = true
                    isPityProcessing = false
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
