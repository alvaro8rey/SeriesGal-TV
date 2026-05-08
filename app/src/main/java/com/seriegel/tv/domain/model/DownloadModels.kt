package com.seriegel.tv.domain.model

enum class DownloadQuality {
    HIGH,
    MEDIUM,
    LOW,
}

enum class DownloadState {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    REMOVING,
    STOPPED,
}

data class DownloadItem(
    val id: String,
    val title: String,
    val streamUrl: String,
    val quality: DownloadQuality,
    val state: DownloadState,
    val progressPercent: Float,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val isSeries: Boolean = true,
    val seriesId: String? = null,
    val episodeId: String? = null,
)

data class DownloadStorageStats(
    val usedBytes: Long,
    val completedItems: Int,
)
