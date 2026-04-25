package com.atpp.rgs.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.R
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
 *
 * Pole [confirmPassword] jest używane TYLKO w trybie REGISTER. W trybie LOGIN
 * jest po prostu ignorowane przez [submit].
 */
data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    /** Komunikat surowy (np. z repozytorium) — gdy nie ma resourceId. */
    val errorMessage: String? = null,
    /** Komunikat z [R.string.*] — pozwala zlokalizować błąd UI bez stringów w VM. */
    val errorRes: Int? = null
)

enum class AuthMode { LOGIN, REGISTER }

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, errorMessage = null, errorRes = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null, errorRes = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, errorRes = null) }

    fun toggleMode() = _uiState.update {
        // Czyścimy potencjalne błędy i pole confirm — żeby nie ciągnąć stanu między trybami.
        it.copy(
            mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
            confirmPassword = "",
            errorMessage = null,
            errorRes = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        // Walidacja confirm-password robiona po stronie VM, nie w repozytorium —
        // to jest stricte sprawa UI/formularza, nie reguła domenowa.
        if (state.mode == AuthMode.REGISTER) {
            if (state.confirmPassword.isBlank()) {
                _uiState.update { it.copy(errorRes = R.string.auth_error_password_required) }
                return
            }
            if (state.password != state.confirmPassword) {
                _uiState.update { it.copy(errorRes = R.string.auth_error_password_mismatch) }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, errorRes = null) }

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
