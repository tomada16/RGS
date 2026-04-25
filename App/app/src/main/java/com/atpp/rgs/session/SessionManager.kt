package com.atpp.rgs.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

/**
 * Trzyma identyfikator zalogowanego użytkownika w DataStore.
 * Dzięki temu sesja przeżywa restart aplikacji.
 *
 * - currentUserId: Flow emitujący aktualne userId lub null gdy wylogowany
 * - login(id): zapisuje id po pomyślnym logowaniu/rejestracji
 * - logout(): czyści sesję
 */
class SessionManager(private val context: Context) {

    private val keyUserId = intPreferencesKey("current_user_id")

    val currentUserId: Flow<Int?> = context.sessionDataStore.data
        .map { prefs -> prefs[keyUserId] }

    suspend fun login(userId: Int) {
        context.sessionDataStore.edit { prefs -> prefs[keyUserId] = userId }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { prefs -> prefs.remove(keyUserId) }
    }
}
