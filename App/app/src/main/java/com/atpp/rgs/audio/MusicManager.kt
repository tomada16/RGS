package com.atpp.rgs.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.atpp.rgs.media.MediaAssets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class MusicManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val keyMasterVolume = floatPreferencesKey("master_volume")
    private val keyMusicVolume  = floatPreferencesKey("music_volume")

    val masterVolume: Flow<Float> = context.settingsDataStore.data
        .map { it[keyMasterVolume] ?: DEFAULT_MASTER }

    val musicVolume: Flow<Float> = context.settingsDataStore.data
        .map { it[keyMusicVolume] ?: DEFAULT_MUSIC }

    private var currentMaster = 0f
    private var currentMusic  = 0f
    private var mediaPlayer: MediaPlayer? = null

    init {
        // NOWOŚĆ: Reaktywne i ciągłe nasłuchiwanie ustawień
        // Gwarantuje, że odtwarzacz zawsze ma aktualną głośność, bez resetów
        scope.launch {
            combine(masterVolume, musicVolume) { master, music ->
                master to music
            }.collect { (master, music) ->
                currentMaster = master
                currentMusic = music
                applyVolume()
            }
        }
    }

    fun start() {
        if (mediaPlayer != null) return
        try {
            val afd = MediaAssets.audioFd(context, "background_music.mp3")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true

                // Najpierw przygotowujemy odtwarzacz...
                prepare()
                // ...a dopiero potem aplikujemy ostateczną głośność, chroniąc się przed nadpisaniem!
                applyVolume()
                start()
            }
        } catch (_: Exception) { /* plik jeszcze niedodany do assets */ }
    }

    fun pause()  { mediaPlayer?.pause() }

    fun resume() {
        if (mediaPlayer?.isPlaying == false) {
            applyVolume() // Asekuracyjne uderzenie głośnością po wybudzeniu
            mediaPlayer?.start()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setMasterVolume(value: Float) {
        currentMaster = value // Optymistyczna aktualizacja UI
        applyVolume()
        scope.launch { context.settingsDataStore.edit { it[keyMasterVolume] = value } }
    }

    fun setMusicVolume(value: Float) {
        currentMusic = value // Optymistyczna aktualizacja UI
        applyVolume()
        scope.launch { context.settingsDataStore.edit { it[keyMusicVolume] = value } }
    }

    private fun applyVolume() {
        val vol = currentMaster * currentMusic
        mediaPlayer?.setVolume(vol, vol)
    }

    companion object {
        const val DEFAULT_MASTER = 0.85f
        const val DEFAULT_MUSIC  = 0.40f
    }
}