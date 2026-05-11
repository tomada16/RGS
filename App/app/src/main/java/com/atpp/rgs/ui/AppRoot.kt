package com.atpp.rgs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.auth.AuthScreen
import com.atpp.rgs.ui.craps.CrapsScreen
import com.atpp.rgs.ui.menu.MainMenuScreen

/**
 * Ekrany dostępne po zalogowaniu.
 * Rozszerz tę sealed class, gdy dodajesz kolejne gry.
 */
sealed class AppScreen {
    /** Główne menu z listą gier. */
    object Menu : AppScreen()

    /** Gra Craps; [gameId] przekazywany z GameEntity. */
    data class Craps(val gameId: Int) : AppScreen()
}

/**
 * Root composable — obserwuje sesję i pokazuje odpowiedni ekran.
 *
 * Stany:
 *  - LOADING_SENTINEL → spinner (DataStore jeszcze nie odpowiedział)
 *  - null             → AuthScreen (użytkownik niezalogowany)
 *  - userId (Int)     → ekran zależny od [AppScreen]
 */
@Composable
fun AppRoot(app: RgsApplication) {
    val sessionState by app.sessionManager.currentUserId
        .collectAsState(initial = LOADING_SENTINEL)

    when (sessionState) {
        LOADING_SENTINEL -> Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        null -> AuthScreen(app = app)

        else -> {
            val userId = requireNotNull(sessionState)

            // Bieżący ekran (zresetowany przy zmianie sesji — np. po wylogowaniu i ponownym logowaniu)
            var currentScreen by remember(userId) { mutableStateOf<AppScreen>(AppScreen.Menu) }

            when (val screen = currentScreen) {

                is AppScreen.Menu -> MainMenuScreen(
                    app        = app,
                    userId     = userId,
                    onJoinGame = { game ->
                        when (game.name.lowercase()) {
                            "craps" -> currentScreen = AppScreen.Craps(game.id)
                            // "blackjack" -> currentScreen = AppScreen.Blackjack(game.id)
                            else -> { /* TODO: pozostałe gry */ }
                        }
                    }
                )

                is AppScreen.Craps -> CrapsScreen(
                    app    = app,
                    userId = userId,
                    onExit = { currentScreen = AppScreen.Menu }
                )
            }
        }
    }
}

private val LOADING_SENTINEL: Int = Int.MIN_VALUE
