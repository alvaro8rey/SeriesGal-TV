package com.seriegel.tv.domain.repository

import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.DownloadQuality
import com.seriegel.tv.domain.model.DownloadStorageStats
import kotlinx.coroutines.flow.Flow

interface DownloadsRepository {
    val downloads: Flow<List<DownloadItem>>

    val activeDownloads: Flow<List<DownloadItem>>

    suspend fun enqueue(
        id: String,
        title: String,
        streamUrl: String,
        quality: DownloadQuality,
        isSeries: Boolean,
        seriesId: String? = null,
        episodeId: String? = null,
    )

    suspend fun remove(id: String)

    suspend fun removeAll()

    suspend fun refresh()

    suspend fun stats(): DownloadStorageStats
}
