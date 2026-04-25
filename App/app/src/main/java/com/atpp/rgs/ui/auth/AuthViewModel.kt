package com.atpp.rgs.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.repository.UserRepository
import com.atpp.rgs.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stan formularza logowania/rejestracji.
 * Trzymany w jednym StateFlow — zgodnie z UDF (Unidirectional Data Flow) z MVVM.
 */
data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class AuthMode { LOGIN, REGISTER }

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
            errorMessage = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = when (state.mode) {
                AuthMode.LOGIN -> userRepository.login(state.username, state.password)
                AuthMode.REGISTER -> userRepository.register(state.username, state.password)
            }
            when (result) {
                is UserRepository.AuthResult.Success -> {
                    sessionManager.login(result.userId)
                    // Po sukcesie SessionManager wyemituje nowe userId,
                    // a AppRoot przełączy ekran. Resetujemy formularz.
                    _uiState.value = AuthUiState()
                }
                is UserRepository.AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    companion object {
        fun factory(app: RgsApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthViewModel(app.userRepository, app.sessionManager) }
        }
    }
}
