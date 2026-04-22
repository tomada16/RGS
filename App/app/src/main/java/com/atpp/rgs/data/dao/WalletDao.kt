package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.atpp.rgs.data.entity.WalletEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets WHERE user_id = :userId")
    fun getWalletByUserId(userId: Int): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE user_id = :userId")
    suspend fun getWalletNow(userId: Int): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET coins = coins + :amount, updated_at = :date WHERE user_id = :userId")
    suspend fun changeCoins(userId: Int, amount: Int, date: Date = Date())
}