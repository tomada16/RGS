package com.atpp.rgs.data.entity

import androidx.compose.ui.unit.Density
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "game_session_id")
    val gameSessionId: Int?,

    @ColumnInfo(name = "amount")
    val amount: Int,
    @ColumnInfo(name = "type")
    val type: String, // "WIN", "LOSS", "BONUS", "PURCHASE"

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)