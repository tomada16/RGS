package com.atpp.rgs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.auth.AuthScreen
import com.atpp.rgs.ui.menu.MainMenuScreen

/**
 * Root composable — obserwuje sesję i pokazuje odpowiedni ekran.
 *
 * Stany:
 *  - userId == null  -> użytkownik niezalogowany -> AuthScreen
 *  - userId != null  -> aktywna sesja -> MainMenuScreen
 *
 * DataStore zwraca pierwszą wartość asynchronicznie. Do tego czasu
 * `initial = null` traktujemy jako "ładowanie" pokazując spinner —
 * inaczej przez ułamek sekundy mignąłby ekran logowania nawet zalogowanym.
 *
 * Aby odróżnić "jeszcze nie wczytano" od "wylogowany", używamy Result-like
 * wrappera: dopóki nie zebraliśmy żadnej wartości, pokazujemy spinner.
 */
@Composable
fun AppRoot(app: RgsApplication) {
    // Sentinel: -1 = jeszcze nie odczytano z DataStore
    val sessionState by app.sessionManager.currentUserId
        .collectAsState(initial = LOADING_SENTINEL)

    when (sessionState) {
        LOADING_SENTINEL -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        null -> AuthScreen(app = app)
        else -> MainMenuScreen(app = app)
    }
}

private val LOADING_SENTINEL: Int = Int.MIN_VALUE
