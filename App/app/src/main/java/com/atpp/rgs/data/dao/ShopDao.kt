package com.atpp.rgs.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.atpp.rgs.data.entity.EquippedItemEntity
import com.atpp.rgs.data.entity.OwnedItemEntity
import com.atpp.rgs.data.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT itemId FROM owned_items WHERE userId = :userId")
    fun getOwnedItems(userId: Int): Flow<List<String>>

    @Query("SELECT * FROM equipped_items WHERE userId = :userId")
    fun getEquippedItems(userId: Int): Flow<List<EquippedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOwnedItem(item: OwnedItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun equipItem(item: EquippedItemEntity)

    // Zabezpieczona transakcja finansowa: odejmie kasę TYLKO, jeśli portfel ma odpowiednie środki
    @Query("UPDATE wallets SET coins = coins - :price WHERE user_id = :userId AND coins >= :price")
    suspend fun deductCoins(userId: Int, price: Int): Int

    @Transaction
    suspend fun buyItemTransaction(userId: Int, itemId: String, price: Int): Boolean {
        val rowsUpdated = deductCoins(userId, price)
        if (rowsUpdated > 0) {
            insertOwnedItem(OwnedItemEntity(userId, itemId))
            return true
        }
        return false // Brak środków, transakcja odrzucona na poziomie bazy!
    }
}