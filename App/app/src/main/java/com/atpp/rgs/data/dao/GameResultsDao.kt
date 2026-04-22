package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.atpp.rgs.data.entity.GameResultsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultsDao {

    @Query("SELECT * FROM game_results ORDER BY id ASC")
    fun getAllResults(): Flow<List<GameResultsEntity>>

    @Query("SELECT * FROM game_results WHERE id = :resultId")
    suspend fun getResultById(resultId: Int): GameResultsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(resultsEntity: GameResultsEntity)

    @Update
    suspend fun updateResult(result: GameResultsEntity)

    @Delete
    suspend fun deleteResult(result: GameResultsEntity)
}