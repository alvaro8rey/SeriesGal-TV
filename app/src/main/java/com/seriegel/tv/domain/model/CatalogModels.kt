package com.seriegel.tv.domain.model

data class Catalog(
    val title: String,
    val series: List<Series>,
    val movies: List<Movie>,
)

data class Series(
    val id: String,
    val title: String,
    val lang: String?,
    val type: String?,
    val description: String?,
    val seasons: List<Season>,
) {
    val allEpisodes: List<Episode>
        get() = seasons.flatMap { it.episodes }
}

data class Season(
    val seasonNumber: Int,
    val title: String,
    val episodes: List<Episode>,
)

data class Episode(
    val id: String,
    val title: String,
    val urlPath: String,
)

data class Movie(
    val id: String,
    val title: String,
    val lang: String?,
    val streamPath: String,
    val type: String?,
    val description: String?,
    val year: String?,
)

data class EpisodeProgress(
    val timeSeconds: Double,
    val durationSeconds: Double,
) {
    val ratio: Float
        get() = if (durationSeconds <= 0.0) 0f else (timeSeconds / durationSeconds).toFloat().coerceIn(0f, 1f)
}

data class ContinueWatchingEntry(
    val seriesId: String,
    val episodeId: String,
    val episodeTitle: String,
    val urlPath: String?,
    val timeSeconds: Double,
    val durationSeconds: Double,
) {
    val ratio: Float
        get() = if (durationSeconds <= 0.0) 0f else (timeSeconds / durationSeconds).toFloat().coerceIn(0f, 1f)
}

data class ProgressUpdate(
    val seriesId: String,
    val episodeId: String,
    val timeSeconds: Double,
    val durationSeconds: Double,
    val episodeTitle: String? = null,
    val urlPath: String? = null,
)
