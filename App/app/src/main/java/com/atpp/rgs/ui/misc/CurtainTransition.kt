package com.atpp.rgs.ui.misc

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CurtainTransition(
    leftLogoResId: Int,
    rightLogoResId: Int,
    onClosed: () -> Unit, // Wywoła się, gdy połówki złączą się na środku
    onOpened: () -> Unit  // Wywoła się na samym końcu, gdy zjadą z ekranu
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val halfWidth = screenWidth / 2

    val logoHeight = 180.dp
    val logoWidth = 90.dp

    // Startujemy od wartości halfWidth (czyli kurtyny schowane po bokach)
    val slideOffset = remember { Animatable(halfWidth.value) }

    LaunchedEffect(Unit) {
        // FAZA 1: Zamykanie kurtyny (od boków do środka)
        slideOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 400,
                easing = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f) // Dynamiczny wjazd
            )
        )

        // Kurtyna zamknęła się, zasłaniając MainMenu. Trigerujemy nawigację w tle!
        onClosed()

        delay(150) // Mikropauza, żeby nowy stół zdążył zainicjować stan bazy danych

        // FAZA 2: Otwieranie kurtyny (ze środka na boki)
        slideOffset.animateTo(
            targetValue = halfWidth.value,
            animationSpec = tween(
                durationMillis = 800,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f) // Eleganckie rozchylanie
            )
        )

        // Koniec całego przejścia
        onOpened()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // LEWA KURTYNA
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(halfWidth)
                .align(Alignment.CenterStart)
                .offset(x = (-slideOffset.value).dp)
                .background(Color(0xFF0F0F0F))
        ) {
            Image(
                painter = painterResource(id = leftLogoResId),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(width = logoWidth, height = logoHeight)
            )
        }

        // PRAWA KURTYNA
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(halfWidth)
                .align(Alignment.CenterEnd)
                .offset(x = (slideOffset.value).dp)
                .background(Color(0xFF0F0F0F))
        ) {
            Image(
                painter = painterResource(id = rightLogoResId),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(width = logoWidth, height = logoHeight)
            )
        }
    }
}