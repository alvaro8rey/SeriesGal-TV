package com.seriegel.tv.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.seriegel.tv.data.local.db.dao.DownloadDao
import com.seriegel.tv.data.local.db.entity.DownloadItemEntity
import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.DownloadQuality
import com.seriegel.tv.domain.model.DownloadState
import com.seriegel.tv.domain.model.DownloadStorageStats
import com.seriegel.tv.domain.repository.DownloadsRepository
import com.seriegel.tv.downloads.DownloadInfrastructure
import com.seriegel.tv.downloads.TvDownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DownloadsRepositoryImpl(
    context: Context,
    private val downloadDao: DownloadDao,
) : DownloadsRepository {
    private val tag = "DownloadsRepository"
    private val appContext = context.applicationContext
    private val infra = DownloadInfrastructure.get(appContext)
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())

    override val downloads: Flow<List<DownloadItem>> = _downloads.asStateFlow()

    override val activeDownloads: Flow<List<DownloadItem>> = _downloads.map { list ->
        list.filter { item ->
            item.state == DownloadState.QUEUED || item.state == DownloadState.DOWNLOADING
        }
    }

    init {
        infra.downloadManager.addListener(
            object : androidx.media3.exoplayer.offline.DownloadManager.Listener {
                override fun onDownloadChanged(
                    downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                    download: Download,
                    finalException: Exception?,
                ) {
                    super.onDownloadChanged(downloadManager, download, finalException)
                    scope.launch { refresh() }
                }

                override fun onDownloadRemoved(
                    downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                    download: Download,
                ) {
                    super.onDownloadRemoved(downloadManager, download)
                    scope.launch { refresh() }
                }
            },
        )
    }

    override suspend fun enqueue(
        id: String,
        title: String,
        streamUrl: String,
        quality: DownloadQuality,
        isSeries: Boolean,
        seriesId: String?,
        episodeId: String?,
    ) {
        Log.d(tag, "enqueue() id=$id quality=${quality.name}")
        val request = DownloadRequest.Builder(id, Uri.parse(streamUrl))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        DownloadService.sendAddDownload(
            appContext,
            TvDownloadService::class.java,
            request,
            false,
        )

        downloadDao.upsert(
            DownloadItemEntity(
                id = id,
                title = title,
                streamUrl = streamUrl,
                quality = quality.name,
                status = DownloadState.QUEUED.name,
                progressPercent = 0f,
                isSeries = isSeries,
                seriesId = seriesId,
                episodeId = episodeId,
            ),
        )

        refresh()
    }

    override suspend fun remove(id: String) {
        Log.d(tag, "remove() id=$id")
        DownloadService.sendRemoveDownload(appContext, TvDownloadService::class.java, id, false)
        downloadDao.deleteById(id)
        refresh()
    }

    override suspend fun removeAll() {
        Log.d(tag, "removeAll()")
        DownloadService.sendRemoveAllDownloads(appContext, TvDownloadService::class.java, false)
        downloadDao.deleteAll()
        refresh()
    }

    override suspend fun refresh() {
        mutex.withLock { refreshFromSources() }
    }

    override suspend fun stats(): DownloadStorageStats {
        val current = _downloads.value
        return DownloadStorageStats(
            usedBytes = infra.usedBytes(),
            completedItems = current.count { it.state == DownloadState.COMPLETED },
        )
    }

    private suspend fun refreshFromSources() {
        Log.d(tag, "refreshFromSources()")
        val managerDownloads = infra.downloadManager.currentDownloads.associateBy { it.request.id }
        val localDownloads = runCatching { downloadDao.getAll() }.getOrDefault(emptyList())
        val merged = localDownloads.map { entity ->
            val remote = managerDownloads[entity.id]
            val resolvedState = remote?.toDomainState() ?: entity.status.toDomainState()
            val progress = when {
                remote == null -> entity.progressPercent
                remote.percentDownloaded < 0f -> 0f
                else -> remote.percentDownloaded
            }
            entity.toDomain().copy(
                state = resolvedState,
                progressPercent = progress,
                bytesDownloaded = remote?.bytesDownloaded ?: entity.bytesDownloaded,
                totalBytes = remote?.contentLength ?: entity.totalBytes,
            )
        }
        _downloads.value = merged
        downloadDao.upsert(merged.map { item -> item.toEntity() })
    }
}

private fun Download.toDomainState(): DownloadState {
    return when (state) {
        Download.STATE_QUEUED -> DownloadState.QUEUED
        Download.STATE_DOWNLOADING -> DownloadState.DOWNLOADING
        Download.STATE_COMPLETED -> DownloadState.COMPLETED
        Download.STATE_FAILED -> DownloadState.FAILED
        Download.STATE_REMOVING -> DownloadState.REMOVING
        Download.STATE_STOPPED -> DownloadState.STOPPED
        else -> DownloadState.QUEUED
    }
}

private fun String.toDomainState(): DownloadState {
    return runCatching { DownloadState.valueOf(this) }.getOrDefault(DownloadState.QUEUED)
}

private fun DownloadItemEntity.toDomain(): DownloadItem {
    return DownloadItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        quality = runCatching { DownloadQuality.valueOf(quality) }.getOrDefault(DownloadQuality.HIGH),
        state = status.toDomainState(),
        progressPercent = progressPercent,
        bytesDownloaded = bytesDownloaded,
        totalBytes = totalBytes,
        isSeries = isSeries,
        seriesId = seriesId,
        episodeId = episodeId,
    )
}

private fun DownloadItem.toEntity(): DownloadItemEntity {
    return DownloadItemEntity(
        id = id,
        title = title,
        streamUrl = streamUrl,
        quality = quality.name,
        status = state.name,
        progressPercent = progressPercent,
        bytesDownloaded = bytesDownloaded,
        totalBytes = totalBytes,
        isSeries = isSeries,
        seriesId = seriesId,
        episodeId = episodeId,
    )
}
