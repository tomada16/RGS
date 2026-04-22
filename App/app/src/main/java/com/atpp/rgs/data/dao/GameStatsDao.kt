package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.atpp.rgs.data.entity.GameStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {

    @Query("SELECT * FROM game_statistics WHERE user_id = :userId")
    fun getStatsByUserId(userId: Int): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_statistics WHERE user_id = :userId AND game_id = :gameId")
    suspend fun getStatsByUserAndGame(userId: Int, gameId: Int): GameStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: GameStatsEntity)

    @Update
    suspend fun updateStats(stats: GameStatsEntity)
}
