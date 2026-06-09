package com.atpp.rgs.data.repository

import com.atpp.rgs.data.GameHistoryRow
import com.atpp.rgs.data.dao.GameDao
import com.atpp.rgs.data.dao.GameResultsDao
import com.atpp.rgs.data.dao.GameSessionDao
import com.atpp.rgs.data.dao.GameStatsDao
import com.atpp.rgs.data.entity.GameResultsEntity
import com.atpp.rgs.data.entity.GameSessionEntity
import com.atpp.rgs.data.entity.GameStatsEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Zapisywanie i odczyt wyników rozegranych rund.
 * Po każdej rozstrzygniętej rundzie gra woła [recordRound], co aktualizuje
 * historię sesji ([GameSessionEntity]) oraz statystyki gracza ([GameStatsEntity]).
 */
class GameRepository(
    private val gameDao: GameDao,
    private val gameResultsDao: GameResultsDao,
    private val gameSessionDao: GameSessionDao,
    private val gameStatsDao: GameStatsDao
) {

    enum class Outcome { WIN, LOSE, PUSH }

    /**
     * @param gameName  nazwa gry zgodna z tabelą `games` (np. "Blackjack", "Craps").
     * @param betAmount całkowita stawka w rundzie.
     * @param netAmount wynik netto rundy (dodatni przy wygranej, ujemny przy przegranej, 0 przy push).
     */
    suspend fun recordRound(
        userId: Int,
        gameName: String,
        outcome: Outcome,
        betAmount: Int,
        netAmount: Int
    ) {
        val gameId = gameDao.getGameByName(gameName)?.id ?: return
        val resultId = resolveResultId(outcome.name)

        gameSessionDao.insertSession(
            GameSessionEntity(
                userId = userId,
                gameId = gameId,
                resultId = resultId,
                betAmount = betAmount
            )
        )

        val base = gameStatsDao.getStatsByUserAndGame(userId, gameId)
            ?: GameStatsEntity(
                userId = userId,
                gameId = gameId,
                totalGames = 0,
                totalWins = 0,
                totalLoses = 0,
                earnedAmount = 0,
                lostAmount = 0
            )

        gameStatsDao.upsertStats(
            base.copy(
                totalGames   = base.totalGames + 1,
                totalWins    = base.totalWins + if (outcome == Outcome.WIN) 1 else 0,
                totalLoses   = base.totalLoses + if (outcome == Outcome.LOSE) 1 else 0,
                earnedAmount = base.earnedAmount + if (outcome == Outcome.WIN) netAmount else 0,
                lostAmount   = base.lostAmount + if (outcome == Outcome.LOSE) -netAmount else 0,
                updatedAt    = Date()
            )
        )
    }

    fun history(userId: Int, limit: Int = 20): Flow<List<GameHistoryRow>> =
        gameSessionDao.getHistoryByUserId(userId, limit)

    fun stats(userId: Int): Flow<List<GameStatsEntity>> =
        gameStatsDao.getStatsByUserId(userId)

    private suspend fun resolveResultId(name: String): Int {
        gameResultsDao.getResultByName(name)?.let { return it.id }
        return gameResultsDao.insertResultReturningId(GameResultsEntity(name = name)).toInt()
    }
}
