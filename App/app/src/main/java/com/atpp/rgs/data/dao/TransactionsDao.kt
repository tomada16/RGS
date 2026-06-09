package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atpp.rgs.data.entity.TransactionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY created_at DESC")
    fun getTransactionsByUserId(userId: Int): Flow<List<TransactionsEntity>>

    @Query("SELECT * FROM transactions WHERE game_session_id = :sessionId ORDER BY created_at DESC")
    fun getTransactionsBySessionId(sessionId: Int): Flow<List<TransactionsEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): TransactionsEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: TransactionsEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionsEntity)
}
