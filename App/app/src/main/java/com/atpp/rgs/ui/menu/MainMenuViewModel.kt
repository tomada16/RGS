package com.atpp.rgs.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.session.SessionManager
import kotlinx.coroutines.launch

class MainMenuViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    fun logout() {
        viewModelScope.launch { sessionManager.logout() }
    }

    companion object {
        fun factory(app: RgsApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainMenuViewModel(app.sessionManager) }
        }
    }
}
