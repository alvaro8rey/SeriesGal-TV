package com.seriegel.tv.downloads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveDownload(
    val id: String,
    val title: String,
    val progress: Float,
)

class DownloadsStateStore {
    private val _activeDownloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val activeDownloads: StateFlow<List<ActiveDownload>> = _activeDownloads.asStateFlow()

    fun replaceAll(items: List<ActiveDownload>) {
        _activeDownloads.value = items
    }
}
