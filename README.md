# RGS — Game Room 🎰

Mobilna aplikacja symulująca gry losowe na Androida napisana w Kotlinie. 
Projekt demonstrujący stos Androida: **Jetpack Compose**, architekturę **MVVM** oraz lokalną bazę danych **Room**.

Gracz zakłada konto, dostaje wirtualny portfel monet i gra w **Blackjacka** oraz **Craps**,
kupuje kosmetyczne motywy w sklepie, śledzi statystyki i historię rozgrywek na swoim profilu.

> Wszystkie środki w grze są wirtualne — aplikacja nie obsługuje płatności ani prawdziwych pieniędzy.

---

## Spis treści

- [Funkcje](#funkcje)
- [Stos technologiczny](#stos-technologiczny)
- [Architektura](#architektura)
- [Model danych (Room)](#model-danych-room)
- [Struktura projektu](#struktura-projektu)
- [Uruchomienie](#uruchomienie)

---

## Funkcje

- **Konta i logowanie** — rejestracja i logowanie z haszowaniem hasła. Sesja jest trwała
  (przeżywa restart aplikacji) dzięki DataStore.
- **Gry:**
  - **Blackjack** — klasyczne 21 z akcjami *Hit / Stand / Double Down / Split*, dobieraniem
    krupiera do 17 i wypłatą 2.5× za blackjacka.
  - **Craps** — zakład *Pass Line* z fazami *come-out* i *point*, animowanym rzutem kości
    i historią rzutów.
- **Portfel i ekonomia** — każdy gracz startuje z saldem monet; zakłady i wygrane aktualizują
  portfel reaktywnie.
- **System „pity"** — gdy saldo spadnie poniżej progu, aplikacja automatycznie dolewa monet,
  żeby gracz mógł grać dalej.
- **Sklep** — kosmetyczne motywy stołów i talii (Blackjack) oraz stołów i kości (Craps).
  Kupione przedmioty można zakładać; wybrany motyw zmienia wygląd stołu w grze.
- **Profil** — nazwa użytkownika, data dołączenia, saldo, zagregowane statystyki
  (rozegrane gry, wygrane, przegrane, win-rate, wynik netto) oraz rozwijana historia gier.
- **Ustawienia i dźwięk** — regulacja głośności głównej i muzyki; muzyka w tle odtwarzana
  przez dedykowany `MusicManager`.

---

## Stos technologiczny

| Obszar            | Technologia |
|-------------------|-------------|
| Język             | Kotlin 2.2.10 |
| UI                | Jetpack Compose (Material 3), Compose BOM 2024.09.00 |
| Architektura      | MVVM + StateFlow |
| Baza danych       | Room 2.8.4 (z KSP) |
| Nawigacja         | Navigation Compose |
| Trwałe preferencje| DataStore Preferences |
| Min / Target SDK  | 24 / 36 |

---

## Architektura

Aplikacja realizuje wzorzec **MVVM** z jednokierunkowym przepływem danych:

```
        ┌──────────────────────────────────────────────┐
        │  View  (Jetpack Compose, @Composable)        │
        │  AuthScreen, MainMenuScreen, BlackjackScreen,│
        │  CrapsScreen, ShopScreen, SettingsScreen,    │
        │  ProfileScreen                               │
        └───────────────────────────────┬──────────────┘
                        ▲               │
            collectAsState()      wywołania akcji
                        │               ▼
        ┌──────────────────────────────────────────────┐
        │  ViewModel  (StateFlow<UiState>)             │
        │  trzyma stan ekranu, logikę gry i akcje      │
        └──────────────────────────────┬──────────────┘
                        ▲              │
                        │              ▼
        ┌──────────────────────────────────────────────┐
        │  Model / Data  (Repository → DAO → Room)     │
        │  UserRepository, GameRepository, AppDatabase │
        └──────────────────────────────────────────────┘
```

- **View** nie zawiera logiki biznesowej — obserwuje `StateFlow` z ViewModelu i wywołuje
  jego metody w reakcji na zdarzenia użytkownika.
- **ViewModel** wystawia niemutowalny stan UI i komunikuje się z warstwą danych przez
  repozytoria/DAO.
- **Service locator** — klasa `RgsApplication` tworzy pojedyncze instancje bazy, repozytoriów,
  `SessionManager` i `MusicManager`.
- **Nawigacja** — `AppRoot` z `NavHost` (ekrany: `auth`, `main_menu`, `blackjack`, `craps`).

---

## Model danych (Room)

Baza `rgs.db` (wersja schematu **2**) zawiera następujące encje:

| Encja                 | Opis |
|-----------------------|------|
| `UserEntity`          | konto użytkownika (nazwa, hasz hasła, data utworzenia) |
| `WalletEntity`        | portfel - saldo monet powiązane 1:1 z użytkownikiem |
| `GameEntity`          | katalog gier (seedowany: Blackjack, Craps) |
| `GameResultsEntity`   | słownik wyników rundy (WIN / LOSE / PUSH) |
| `GameSessionEntity`   | pojedyncza rozegrana runda (gra, wynik, stawka, data) |
| `GameStatsEntity`     | zagregowane statystyki gracza per gra |
| `TransactionsEntity`  | rejestr transakcji portfela |
| `OwnedItemEntity`     | przedmioty kupione w sklepie |
| `EquippedItemEntity`  | aktualnie założone przedmioty (per kategoria) |

- Dostęp przez interfejsy **DAO** (`@Dao`) zwracające `Flow` dla reaktywnego UI.
- **Migracja 1 → 2** dodaje tabele sklepu (`owned_items`, `equipped_items`).
- `Callback` przy tworzeniu bazy **seeduje** listę gier.
- Konwertery typów (`Converters`) mapują `Date` <-> `Long`.

Po rozstrzygnięciu rundy gra wywołuje `GameRepository.recordRound(...)`, co zapisuje sesję
i aktualizuje statystyki.

---

## Struktura projektu

```
RGS/
├─ README.md
└─ App/                         ← projekt Android Studio (Gradle)
   └─ app/src/main/
      ├─ java/com/atpp/rgs/
      │  ├─ RgsApplication.kt    ← service locator
      │  ├─ MainActivity.kt
      │  ├─ data/                ← Room: entity/, dao/, 
      │  │                         repository/, AppDatabase
      │  ├─ session/             ← SessionManager (DataStore)
      │  ├─ audio/               ← MusicManager
      │  ├─ media/               ← dostęp do assetów
      │  ├─ model/               ← modele domenowe (np. Card)
      │  └─ ui/                  ← Compose: auth, menu, blackjack, 
      │                            craps, shop, settings, profile, 
      │                            theme, components
      │                             
      └─ res/                    ← zasoby (strings, drawable)
```

---

## Uruchomienie

Wymagania: **Android Studio** (najnowsza stabilna wersja) oraz JDK 11.

1. Otwórz folder `App/` jako projekt w Android Studio.
2. Poczekaj na synchronizację Gradle (pobranie zależności).
3. Uruchom konfigurację **app** na emulatorze lub urządzeniu z **Androidem 7.0 (API 24)** lub nowszym.
