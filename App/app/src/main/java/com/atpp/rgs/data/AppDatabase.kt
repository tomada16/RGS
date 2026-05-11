package com.atpp.rgs.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

    companion object {
        /**
         * Callback wywoływany raz przy pierwszym tworzeniu bazy danych.
         * Wstawia predefiniowane gry za pomocą surowego SQL (synchronicznie,
         */
        val seedCallback: Callback = object : Callback() {

            /** Nowa instalacja — baza właśnie powstała, wstawiamy gry. */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertSeedGames(db)
            }

            /**
             * Każde otwarcie bazy — wstawiamy gry tylko jeśli tabela jest pusta.
             * Obsługuje przypadek gdy baza istniała przed dodaniem seedów
             * (onCreate nie odpala się ponownie przy aktualizacji aplikacji).
             */
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

        private val SEED_GAMES = listOf(
            "Blackjack" to "Classic 21 with elite side bets and high-roller limits",
            "Craps"     to "The heartbeat of the casino floor. Master the dice"
        )
    }
}