package com.atpp.rgs.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.atpp.rgs.data.entity.*
import com.atpp.rgs.data.dao.*

@Database(
    entities = [
        UserEntity::class,
        GameEntity::class,
        GameStatsEntity::class,
        GameResultsEntity::class,
        GameSessionEntity::class,
        WalletEntity::class,
        TransactionsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun gameDao(): GameDao
    abstract fun gameResultsDao(): GameResultsDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun transactionsDao(): TransactionsDao
}