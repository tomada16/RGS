package com.atpp.rgs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.atpp.rgs.ui.AppRoot
import com.atpp.rgs.ui.theme.RGSTheme

class MainActivity : ComponentActivity() {

    private val app get() = application as RgsApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RGSTheme {
                AppRoot(app = app)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        app.musicManager.start()
        app.musicManager.resume()
    }

    override fun onPause() {
        super.onPause()
        app.musicManager.pause()
    }
}