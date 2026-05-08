package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val tokenPreview: String = "",
    val preferredQuality: String = "HIGH",
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val sessionDataStore = container.sessionDataStore

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val token = sessionDataStore.token.first().orEmpty()
            val quality = sessionDataStore.preferredQuality.first()
            _uiState.value = ProfileUiState(
                tokenPreview = token.take(12).plus(if (token.length > 12) "..." else ""),
                preferredQuality = quality,
            )
        }
    }

    fun cyclePreferredQuality() {
        viewModelScope.launch {
            val next = when (_uiState.value.preferredQuality) {
                "HIGH" -> "MEDIUM"
                "MEDIUM" -> "LOW"
                else -> "HIGH"
            }
            sessionDataStore.setPreferredQuality(next)
            _uiState.value = _uiState.value.copy(preferredQuality = next)
        }
    }
}
