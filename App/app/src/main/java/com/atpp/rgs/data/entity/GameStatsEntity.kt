package com.atpp.rgs.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "game_statistics",
    primaryKeys = ["user_id", "game_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"]
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"]
        )
    ])
data class GameStatsEntity(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "game_id")
    val gameId: Int,

    @ColumnInfo(name = "total_games")
    val totalGames: Int,
    @ColumnInfo(name = "total_wins")
    val totalWins: Int,
    @ColumnInfo(name = "total_loses")
    val totalLoses: Int,

    @ColumnInfo(name = "earned_amount")
    val earnedAmount: Int,
    @ColumnInfo(name = "lost_amount")
    val lostAmount: Int,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)