package com.atpp.rgs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.auth.AuthScreen
import com.atpp.rgs.ui.blackjack.BlackjackScreen
import com.atpp.rgs.ui.menu.MainMenuScreen

/**
 * Root composable — obserwuje stan sesji użytkownika i zarządza głównym
 * drzewem nawigacji (NavHost) w całej aplikacji.
 *
 * Stany sesji:
 * - sessionState == LOADING_SENTINEL -> Odczyt asynchroniczny z DataStore -> Pokazuje spinner ładowania
 * - sessionState == null             -> Użytkownik niezalogowany          -> Przekierowanie do ekranu logowania (auth)
 * - sessionState != null             -> Aktywna sesja (poprawny userId)   -> Start w Menu Głównym (main_menu)
 *
 * Wykorzystuje Jetpack Compose Navigation do płynnego przechodzenia między modułami,
 * w tym bezpiecznego powrotu do ekranu logowania po wywołaniu akcji wylogowania.
 */
@Composable
fun AppRoot(app: RgsApplication) {
    val navController = rememberNavController()

    // Obserwujemy sesję użytkownika z DataStore
    val sessionState by app.sessionManager.currentUserId
        .collectAsState(initial = LOADING_SENTINEL)

    // Reagujemy na zmianę stanu sesji (np. kliknięcie przycisku Logout w profilu)
    LaunchedEffect(sessionState) {
        if (sessionState == null) {
            // Jeśli użytkownik się wylogował, czyścimy cały stos ekranów i wracamy do autoryzacji
            navController.navigate("auth") {
                popUpTo(0)
            }
        }
    }

    when (sessionState) {
        LOADING_SENTINEL -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            // Definiujemy kompletne drzewo nawigacji aplikacji
            NavHost(
                navController = navController,
                // Automatycznie dobieramy ekran startowy na podstawie obecności aktywnej sesji
                startDestination = if (sessionState == null) "auth" else "main_menu"
            ) {
                // Ekran Logowania i Rejestracji
                composable("auth") {
                    AuthScreen(app = app)
                }

                // Ekran Menu Głównego (VIP Lounge)
                composable("main_menu") {
                    val userId = sessionState ?: return@composable
                    MainMenuScreen(
                        app = app,
                        userId = userId,
                        onNavigateToBlackjack = {
                            navController.navigate("blackjack")
                        }
                    )
                }

                // Moduł gry: Ekran Blackjacka (Kasyno)
                composable("blackjack") {
                    BlackjackScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

// Kompilator traktuje const val optymalniej przy sprawdzaniu sentinelów w Compose
private const val LOADING_SENTINEL: Int = Int.MIN_VALUE