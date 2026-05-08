package com.seriegel.tv.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ProgressPayloadDto(
    @JsonNames("series_id", "seriesId")
    val seriesId: String,
    @JsonNames("episode_id", "episodeId")
    val episodeId: String,
    val time: Double,
    val duration: Double,
    @JsonNames("episode_title", "episodeTitle")
    val episodeTitle: String? = null,
    val url: String? = null,
)

@Serializable
data class ProgressResponseDto(
    @JsonNames("time", "current_time", "position")
    val time: Double? = null,
    @JsonNames("duration", "total_duration")
    val duration: Double? = null,
)

@Serializable
data class WatchingEntryDto(
    @JsonNames("series_id", "seriesId", "series")
    val seriesId: String? = null,
    @JsonNames("episode_id", "episodeId", "episode")
    val episodeId: String? = null,
    @JsonNames("episode_title", "episodeTitle", "title")
    val episodeTitle: String? = null,
    @JsonNames("url", "episode_url")
    val url: String? = null,
    @JsonNames("time", "current_time", "position")
    val time: Double? = null,
    @JsonNames("duration", "total_duration")
    val duration: Double? = null,
)
