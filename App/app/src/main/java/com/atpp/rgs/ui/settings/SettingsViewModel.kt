package com.atpp.rgs.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.audio.MusicManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val app: RgsApplication) : ViewModel() {

    val masterVolume: StateFlow<Float> = app.musicManager.masterVolume
        .stateIn(viewModelScope, SharingStarted.Lazily, MusicManager.DEFAULT_MASTER)

    val musicVolume: StateFlow<Float> = app.musicManager.musicVolume
        .stateIn(viewModelScope, SharingStarted.Lazily, MusicManager.DEFAULT_MUSIC)

    fun setMasterVolume(value: Float) = app.musicManager.setMasterVolume(value)
    fun setMusicVolume(value: Float)  = app.musicManager.setMusicVolume(value)

    companion object {
        fun factory(app: RgsApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(app) }
        }
    }
}
