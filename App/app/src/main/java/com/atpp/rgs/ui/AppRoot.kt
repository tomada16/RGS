package com.atpp.rgs.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.auth.AuthScreen
import com.atpp.rgs.ui.blackjack.BlackjackScreen
import com.atpp.rgs.ui.craps.CrapsScreen
import com.atpp.rgs.ui.menu.MainMenuScreen
import com.atpp.rgs.ui.misc.CurtainTransition

@Composable
fun AppRoot(app: RgsApplication) {
    val navController = rememberNavController()

    val sessionState by app.sessionManager.currentUserId
        .collectAsState(initial = LOADING_SENTINEL)

    // STANY GLOBALNEJ KURTYNY
    var showCurtain by remember { mutableStateOf(false) }
    var pendingRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionState) {
        if (sessionState == null) {
            navController.navigate("auth") {
                popUpTo(0)
            }
        }
    }

    // --- FUNKCJE STERUJĄCE ---
    // 1. Zmiana stołu z kurtyną
    val navigateWithCurtain = { route: String ->
        pendingRoute = route
        showCurtain = true
    }

    // 2. NOWOŚĆ: Powrót do menu z kurtyną
    val navigateBackWithCurtain = {
        pendingRoute = "BACK" // Używamy "BACK" jako flagi cofania
        showCurtain = true
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Warstwa 1 (Na dole): Zawartość aplikacji i ekrany
        when (sessionState) {
            LOADING_SENTINEL -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                NavHost(
                    navController = navController,
                    startDestination = if (sessionState == null) "auth" else "main_menu",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable("auth") {
                        AuthScreen(app = app)
                    }

                    composable("main_menu") {
                        val userId = sessionState ?: return@composable
                        MainMenuScreen(
                            app = app,
                            userId = userId,
                            onNavigateToBlackjack = { navigateWithCurtain("blackjack") },
                            onNavigateToCraps = { navigateWithCurtain("craps") }
                        )
                    }

                    composable("blackjack") {
                        val userId = sessionState ?: return@composable
                        BlackjackScreen(
                            app = app,
                            userId = userId,
                            // Zamiast popBackStack(), odpalamy naszą funkcję kurtyny!
                            onNavigateBack = { navigateBackWithCurtain() }
                        )
                    }

                    composable("craps") {
                        val userId = sessionState ?: return@composable
                        CrapsScreen(
                            app = app,
                            userId = userId,
                            // Zamiast popBackStack(), odpalamy naszą funkcję kurtyny!
                            onExit = { navigateBackWithCurtain() }
                        )
                    }
                }
            }
        }

        // Warstwa 2 (Na wierzchu): Kurtyna VIP
        if (showCurtain) {
            CurtainTransition(
                leftLogoResId = R.drawable.ic_logo_left,
                rightLogoResId = R.drawable.ic_logo_right,
                onClosed = {
                    // Kiedy logo łączy się na środku, sprawdzamy polecenie:
                    if (pendingRoute == "BACK") {
                        navController.popBackStack() // Cofa do menu
                    } else {
                        pendingRoute?.let { route ->
                            navController.navigate(route) // Wchodzi na stół
                        }
                    }
                },
                onOpened = {
                    showCurtain = false
                    pendingRoute = null
                }
            )
        }
    }
}

private const val LOADING_SENTINEL: Int = Int.MIN_VALUE