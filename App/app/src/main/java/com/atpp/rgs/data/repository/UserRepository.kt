package com.atpp.rgs.data.repository

import com.atpp.rgs.data.dao.UserDao
import com.atpp.rgs.data.dao.WalletDao
import com.atpp.rgs.data.entity.UserEntity
import com.atpp.rgs.data.entity.WalletEntity
import com.atpp.rgs.util.PasswordHasher

/**
 * Warstwa pośrednia między DAO a ViewModelem.
 * Tu trzymamy logikę domenową (hashowanie hasła, tworzenie portfela przy rejestracji).
 */
class UserRepository(
    private val userDao: UserDao,
    private val walletDao: WalletDao
) {

    sealed class AuthResult {
        data class Success(val userId: Int) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    suspend fun register(username: String, password: String): AuthResult {
        val name = username.trim()
        if (name.isBlank() || password.isBlank()) {
            return AuthResult.Error("Login i hasło nie mogą być puste")
        }
        if (password.length < 4) {
            return AuthResult.Error("Hasło musi mieć min. 4 znaki")
        }
        if (userDao.getUserByUsername(name) != null) {
            return AuthResult.Error("Użytkownik o tej nazwie już istnieje")
        }

        val newId = userDao.insertUser(
            UserEntity(username = name, passwordHash = PasswordHasher.hash(password))
        )
        if (newId == -1L) {
            return AuthResult.Error("Nie udało się utworzyć konta")
        }

        // Każdy nowy użytkownik dostaje portfel z domyślnym saldem
        walletDao.insertWallet(WalletEntity(userId = newId.toInt()))

        return AuthResult.Success(newId.toInt())
    }

    suspend fun login(username: String, password: String): AuthResult {
        val name = username.trim()
        val user = userDao.getUserByUsername(name)
            ?: return AuthResult.Error("Nieprawidłowy login lub hasło")

        return if (user.passwordHash == PasswordHasher.hash(password)) {
            AuthResult.Success(user.id)
        } else {
            AuthResult.Error("Nieprawidłowy login lub hasło")
        }
    }

    suspend fun getUserById(id: Int): UserEntity? = userDao.getUserById(id)

    suspend fun checkAndGrantPity(userId: Int): Boolean {
        val coins = walletDao.getWalletNow(userId)?.coins ?: return false
        return if (coins < PITY_THRESHOLD) {
            walletDao.changeCoins(userId, PITY_AMOUNT)
            true
        } else false
    }

    companion object {
        const val PITY_THRESHOLD = 10
        const val PITY_AMOUNT = 100
    }
}
