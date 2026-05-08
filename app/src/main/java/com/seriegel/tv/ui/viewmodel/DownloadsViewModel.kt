package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.DownloadStorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DownloadsUiState(
    val downloads: List<DownloadItem> = emptyList(),
    val stats: DownloadStorageStats = DownloadStorageStats(usedBytes = 0, completedItems = 0),
)

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadsRepository = (application as TvApplication).appContainer.downloadsRepository

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadsRepository.downloads.collect { items ->
                _uiState.update { it.copy(downloads = items) }
                refreshStats()
            }
        }
        viewModelScope.launch { downloadsRepository.refresh() }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(stats = downloadsRepository.stats()) }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch { downloadsRepository.remove(id) }
    }

    fun clearAll() {
        viewModelScope.launch { downloadsRepository.removeAll() }
    }
}
