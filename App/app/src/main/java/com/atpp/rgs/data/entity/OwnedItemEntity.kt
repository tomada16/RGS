package com.atpp.rgs.data.entity

import androidx.room.Entity

@Entity(tableName = "owned_items", primaryKeys = ["userId", "itemId"])
data class OwnedItemEntity(
    val userId: Int,
    val itemId: String
)