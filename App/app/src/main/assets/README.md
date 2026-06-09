# Assets

Folder na zasoby medialne dołączane do APK. Dostęp przez `AssetManager`
(`context.assets`) lub helper `com.atpp.rgs.media.MediaAssets`.

## Struktura

- `images/` — obrazki (png, jpg, webp). Akceptują podfoldery (np. `images/games/dice/bg.webp`).
- `audio/`  — pliki audio (mp3, ogg, wav).
- `video/`  — pliki wideo (mp4, webm).

## Kiedy `assets/` vs `res/raw/` vs `res/drawable/`

| Typ                                | Miejsce            | Dostęp                                 |
|------------------------------------|--------------------|----------------------------------------|
| Ikony, grafiki UI, vector drawable | `res/drawable/`    | `painterResource(R.drawable.xxx)`      |
| Krótkie SFX (id-owalne z R.raw)    | `res/raw/`         | `MediaPlayer.create(ctx, R.raw.xxx)`   |
| Wiele obrazków pogrupowanych       | `assets/images/`   | `MediaAssets.openImage(...)`           |
| Muzyka / długie audio              | `assets/audio/`    | `MediaAssets.audioFd(...)` + MediaPlayer |
| Wideo                              | `assets/video/`    | `MediaAssets.videoFd(...)` + MediaPlayer |

## Pliki audio — atrybucja

| Plik | Tytuł | Autor | Licencja |
|------|-------|-------|----------|
| `audio/background_music.mp3` | Lo-Fi Music Loop – Sentimental Jazzy Love | Sonican | [Pixabay License](https://pixabay.com/music/lofi-lo-fi-music-loop-sentimental-jazzy-love-473154/) |

## Ograniczenia

- **`res/raw/`**: nazwy plików tylko `[a-z0-9_]`, bez podfolderów. Ale dostępne przez `R.raw.*` z autouzupełnianiem.
- **`assets/`**: dowolne nazwy i podfoldery. Brak typowania w czasie kompilacji — odwołujemy się przez ścieżki tekstowe.
