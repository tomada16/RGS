package com.atpp.rgs

import android.app.Application
import androidx.room.Room
import com.atpp.rgs.audio.MusicManager
import com.atpp.rgs.data.AppDatabase
import com.atpp.rgs.data.repository.GameRepository
import com.atpp.rgs.data.repository.UserRepository
import com.atpp.rgs.session.SessionManager

/**
 * Application class — pełni rolę prostego "service locatora".
 * Bez Hilta — instancje DB, repozytoriów i SessionManagera tworzymy tu raz.
 * ViewModele dostają je przez własną Factory.
 */
class RgsApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "rgs.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addCallback(AppDatabase.seedCallback)
            .build()
    }

    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao(), database.walletDao())
    }

    val gameRepository: GameRepository by lazy {
        GameRepository(
            database.gameDao(),
            database.gameResultsDao(),
            database.gameSessionDao(),
            database.gameStatsDao()
        )
    }

    val sessionManager: SessionManager by lazy { SessionManager(this) }

    val musicManager: MusicManager by lazy { MusicManager(this) }
}
