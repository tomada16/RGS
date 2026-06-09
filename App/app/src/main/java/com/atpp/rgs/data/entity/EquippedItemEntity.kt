package com.atpp.rgs.data.entity

import androidx.room.Entity

@Entity(tableName = "equipped_items", primaryKeys = ["userId", "category"])
data class EquippedItemEntity(
    val userId: Int,
    val category: String, // np. "BJ_TABLE", "CRAPS_DICE"
    val itemId: String
)