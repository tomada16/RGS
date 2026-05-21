package com.atpp.rgs.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        TransactionsEntity::class,
        // --- NOWE ENCJE SKLEPU ---
        OwnedItemEntity::class,
        EquippedItemEntity::class
    ],
    version = 2, // <--- ZMIENIONE Z 1 NA 2!
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

    // --- NOWE DAO SKLEPU ---
    abstract fun shopDao(): ShopDao

    companion object {
        val seedCallback: Callback = object : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertSeedGames(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                val cursor = db.query("SELECT COUNT(*) FROM games")
                val isEmpty = cursor.use { c -> c.moveToFirst() && c.getInt(0) == 0 }
                if (isEmpty) insertSeedGames(db)
            }

            private fun insertSeedGames(db: SupportSQLiteDatabase) {
                SEED_GAMES.forEach { (name, description) ->
                    db.execSQL(
                        "INSERT INTO games (name, description) VALUES (?, ?)",
                        arrayOf(name, description)
                    )
                }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `owned_items` " +
                    "(`userId` INTEGER NOT NULL, `itemId` TEXT NOT NULL, " +
                    "PRIMARY KEY(`userId`, `itemId`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `equipped_items` " +
                    "(`userId` INTEGER NOT NULL, `category` TEXT NOT NULL, `itemId` TEXT NOT NULL, " +
                    "PRIMARY KEY(`userId`, `category`))"
                )
            }
        }

        private val SEED_GAMES = listOf(
            "Blackjack" to "Classic 21 with elite side bets and high-roller limits",
            "Craps"     to "The heartbeat of the casino floor. Master the dice"
        )
    }
}