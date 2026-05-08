package com.seriegel.tv.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_items")
data class DownloadItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val streamUrl: String,
    val progress: Float = 0f,
    val quality: String = "high",
    val status: String = "queued",
)
