package com.atpp.rgs.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.GameHistoryRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/** Kompletny, niemutowalny stan ekranu Profile. */
data class ProfileUiState(
    val username: String = "",
    val memberSince: Date? = null,
    val coins: Int = 0,

    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val totalLoses: Int = 0,
    /** Procent wygranych spośród rozstrzygniętych gier (0–100). */
    val winRate: Int = 0,
    /** Wynik netto: suma wygranych minus suma przegranych. */
    val netResult: Int = 0,

    val history: List<GameHistoryRow> = emptyList()
)

class ProfileViewModel(
    private val app: RgsApplication,
    private val userId: Int
) : ViewModel() {

    private val user = MutableStateFlow<com.atpp.rgs.data.entity.UserEntity?>(null)

    init {
        viewModelScope.launch {
            user.value = app.userRepository.getUserById(userId)
        }
    }

    val state: StateFlow<ProfileUiState> = combine(
        user,
        app.database.walletDao().getWalletByUserId(userId),
        app.gameRepository.stats(userId),
        app.gameRepository.history(userId, limit = 100)
    ) { user, wallet, stats, history ->
        val wins  = stats.sumOf { it.totalWins }
        val loses = stats.sumOf { it.totalLoses }
        val decided = wins + loses

        ProfileUiState(
            username    = user?.username ?: "",
            memberSince = user?.createdAt,
            coins       = wallet?.coins ?: 0,
            totalGames  = stats.sumOf { it.totalGames },
            totalWins   = wins,
            totalLoses  = loses,
            winRate     = if (decided > 0) wins * 100 / decided else 0,
            netResult   = stats.sumOf { it.earnedAmount } - stats.sumOf { it.lostAmount },
            history     = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun logout() {
        viewModelScope.launch { app.sessionManager.logout() }
    }

    companion object {
        fun factory(app: RgsApplication, userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfileViewModel(app, userId) }
        }
    }
}
