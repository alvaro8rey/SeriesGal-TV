package com.seriegel.tv.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(
    val title: String = "",
    val series: List<SeriesDto> = emptyList(),
    val movies: List<MovieDto> = emptyList(),
)

@Serializable
data class SeriesDto(
    val id: String,
    val title: String,
    val lang: String? = null,
    val type: String? = null,
    val seasons: List<SeasonDto>? = null,
    val episodes: List<EpisodeDto>? = null,
    val description: String? = null,
)

@Serializable
data class SeasonDto(
    val season: Int = 1,
    val title: String = "",
    val episodes: List<EpisodeDto> = emptyList(),
)

@Serializable
data class EpisodeDto(
    val id: String,
    val title: String,
    val url: String,
)

@Serializable
data class MovieDto(
    val id: String,
    val title: String,
    val lang: String? = null,
    val url: String,
    val type: String? = null,
    val description: String? = null,
    val year: String? = null,
)
