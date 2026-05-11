package com.atpp.rgs.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.entity.GameEntity
import com.atpp.rgs.data.entity.WalletEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun logout() {
        viewModelScope.launch { app.sessionManager.logout() }
    }

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainMenuViewModel(app, userId) }
        }
    }
}
