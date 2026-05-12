package com.atpp.rgs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.atpp.rgs.ui.AppRoot
import com.atpp.rgs.ui.theme.RGSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as RgsApplication
        setContent {
            RGSTheme {
                // Usunięty zduplikowany Scaffold!
                // Teraz ekrany wewnątrz (jak MainMenuScreen) same zarządzą swoimi marginesami.
                AppRoot(app = app)
            }
        }
    }
}