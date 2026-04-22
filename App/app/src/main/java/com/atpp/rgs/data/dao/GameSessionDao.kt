package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atpp.rgs.data.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Query("SELECT * FROM game_session ORDER BY played_at DESC")
    fun getAllSessions(): Flow<List<GameSessionEntity>>

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
