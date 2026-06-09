package com.atpp.rgs.data

import java.util.Date

/**
 * Wiersz historii gier — wynik JOIN-a game_session × games × game_results.
 * Nie jest encją Room; służy tylko jako projekcja zapytania w [GameSessionDao].
 */
data class GameHistoryRow(
    val gameName: String,
    val resultName: String?,
    val betAmount: Int,
    val playedAt: Date
)
