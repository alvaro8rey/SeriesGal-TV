@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.seriegel.tv.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonNames

@Serializable
data class ProgressPayloadDto(
    @SerialName("series_id")
    @JsonNames("series_id", "seriesId")
    val seriesId: String,
    @SerialName("episode_id")
    @JsonNames("episode_id", "episodeId")
    val episodeId: String,
    val time: Double,
    val duration: Double,
    @SerialName("episode_title")
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
