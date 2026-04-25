package com.atpp.rgs.media

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
import java.io.InputStream

/**
 * Centralny punkt dostępu do plików w `app/src/main/assets/`.
 *
 * Konwencja katalogów:
 *  - assets/images/  — obrazki (png/jpg/webp)
 *  - assets/audio/   — pliki audio (mp3/ogg/wav)
 *  - assets/video/   — pliki wideo (mp4/webm)
 *
 * Dla grafik UI i ikon używamy `res/drawable/` z `painterResource(R.drawable.x)`.
 * Dla krótkich SFX można użyć `res/raw/` z `MediaPlayer.create(ctx, R.raw.x)`.
 */
object MediaAssets {

    private const val DIR_IMAGES = "images"
    private const val DIR_AUDIO = "audio"
    private const val DIR_VIDEO = "video"

    // ---------- niskopoziomowe ----------

    /** Otwiera dowolny plik z assets. Ścieżka relatywna do `assets/`, np. `images/logo.png`. */
    fun open(context: Context, path: String): InputStream =
        context.assets.open(path)

    /**
     * Zwraca [AssetFileDescriptor] dla MediaPlayera.
     * MediaPlayer/ExoPlayer potrzebuje FD a nie InputStream żeby móc seekować.
     */
    fun fd(context: Context, path: String): AssetFileDescriptor =
        context.assets.openFd(path)

    /** Listuje pliki w katalogu (bez rekursji). Zwraca pustą listę gdy folder nie istnieje. */
    fun list(context: Context, dir: String): List<String> =
        context.assets.list(dir)?.toList() ?: emptyList()

    // ---------- obrazy ----------

    /** Wczytuje obraz z `assets/images/<name>` jako [Bitmap]. */
    fun loadImageBitmap(context: Context, name: String): Bitmap? {
        return try {
            open(context, "$DIR_IMAGES/$name").use { BitmapFactory.decodeStream(it) }
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Compose helper — pamięta dekodowany [ImageBitmap] między rekompozycjami.
     * Użycie:
     * ```
     * val img = rememberAssetImage("games/dice/bg.webp") ?: return
     * Image(bitmap = img, contentDescription = null)
     * ```
     */
    @Composable
    fun rememberAssetImage(name: String): ImageBitmap? {
        val context = LocalContext.current
        return remember(name) { loadImageBitmap(context, name)?.asImageBitmap() }
    }

    // ---------- audio ----------

    /** [AssetFileDescriptor] dla pliku z `assets/audio/<name>` — do podania MediaPlayerowi. */
    fun audioFd(context: Context, name: String): AssetFileDescriptor =
        fd(context, "$DIR_AUDIO/$name")

    fun listAudio(context: Context): List<String> = list(context, DIR_AUDIO)

    // ---------- wideo ----------

    /** [AssetFileDescriptor] dla pliku z `assets/video/<name>`. */
    fun videoFd(context: Context, name: String): AssetFileDescriptor =
        fd(context, "$DIR_VIDEO/$name")

    fun listVideo(context: Context): List<String> = list(context, DIR_VIDEO)
}
