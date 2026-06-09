package com.atpp.rgs.util

import java.security.MessageDigest

/**
 * Prosty hasher SHA-256. To NIE jest zabezpieczenie produkcyjne (brak soli, brak KDF),
 * ale wystarczy na potrzeby projektu — nie zapisujemy haseł plain-text w bazie.
 */
object PasswordHasher {

    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, hash: String): Boolean = hash(password) == hash
}
