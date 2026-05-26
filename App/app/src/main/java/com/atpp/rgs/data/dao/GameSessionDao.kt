package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atpp.rgs.data.GameHistoryRow
import com.atpp.rgs.data.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Query("SELECT * FROM game_session ORDER BY played_at DESC")
    fun getAllSessions(): Flow<List<GameSessionEntity>>

    @Query(
        """
        SELECT g.name AS gameName, r.name AS resultName,
               s.bet_amount AS betAmount, s.played_at AS playedAt
        FROM game_session s
        JOIN games g ON g.id = s.game_id
        LEFT JOIN game_results r ON r.id = s.result_id
        WHERE s.user_id = :userId
        ORDER BY s.played_at DESC
        LIMIT :limit
        """
    )
    fun getHistoryByUserId(userId: Int, limit: Int): Flow<List<GameHistoryRow>>

    @Query("SELECT * FROM game_session WHERE user_id = :userId ORDER BY played_at DESC")
    fun getSessionsByUserId(userId: Int): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_session WHERE game_id = :gameId ORDER BY played_at DESC")
    fun getSessionsByGameId(gameId: Int): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_session WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): GameSessionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: GameSessionEntity): Long

    @Delete
    suspend fun deleteSession(session: GameSessionEntity)
}
