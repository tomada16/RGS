package com.atpp.rgs

import android.app.Application
import androidx.room.Room
import com.atpp.rgs.data.AppDatabase
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
            .build()
    }

    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao(), database.walletDao())
    }

    val sessionManager: SessionManager by lazy { SessionManager(this) }
}
