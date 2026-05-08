package com.seriegel.tv.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_items")
data class DownloadItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val streamUrl: String,
    val quality: String = "HIGH",
    val status: String = "QUEUED",
    val progressPercent: Float = 0f,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val isSeries: Boolean = true,
    val seriesId: String? = null,
    val episodeId: String? = null,
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
)
