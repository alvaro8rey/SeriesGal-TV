package com.seriegel.tv.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_cache")
data class ProgressCacheEntity(
    @PrimaryKey val id: String,
    val seriesId: String,
    val episodeId: String,
    val timeSeconds: Double,
    val durationSeconds: Double,
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
)
