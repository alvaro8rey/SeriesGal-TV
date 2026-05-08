package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.domain.repository.SessionValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data object LoggedOut : SessionUiState
    data class LoggedIn(val offlineMode: Boolean) : SessionUiState
}

class AppSessionViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val authRepository = container.authRepository

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.token.collectLatest { token ->
                if (token.isNullOrBlank()) {
                    _uiState.value = SessionUiState.LoggedOut
                } else {
                    when (authRepository.validateSession(token)) {
                        SessionValidationResult.Valid -> _uiState.value = SessionUiState.LoggedIn(offlineMode = false)
                        SessionValidationResult.NetworkError -> _uiState.value = SessionUiState.LoggedIn(offlineMode = true)
                        SessionValidationResult.Unauthorized -> {
                            authRepository.logout()
                            _uiState.value = SessionUiState.LoggedOut
                        }

                        is SessionValidationResult.UnknownError -> _uiState.value = SessionUiState.LoggedIn(offlineMode = true)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = SessionUiState.LoggedOut
        }
    }
}
