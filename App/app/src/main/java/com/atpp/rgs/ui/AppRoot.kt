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

@Composable
fun AppRoot(app: RgsApplication) {
    val navController = rememberNavController()

    // Obserwujemy sesję
    val sessionState by app.sessionManager.currentUserId
        .collectAsState(initial = LOADING_SENTINEL)

    // Reagujemy na zmianę stanu sesji (automatyczne wylogowanie/zalogowanie)
    LaunchedEffect(sessionState) {
        if (sessionState == null) {
            // Jeśli użytkownik się wylogował, czyścimy stos i wracamy do logowania
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
            // Definiujemy drzewo nawigacji
            NavHost(
                navController = navController,
                // Jeśli sessionState jest null, startujemy od logowania, inaczej od menu
                startDestination = if (sessionState == null) "auth" else "main_menu"
            ) {
                // Ekran Logowania
                composable("auth") {
                    AuthScreen(app = app)
                }

                // Ekran Menu Głównego
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

                // TWÓJ MODUŁ: Ekran Blackjacka
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

private const val LOADING_SENTINEL: Int = Int.MIN_VALUE