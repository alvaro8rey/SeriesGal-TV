package com.seriegel.tv.player

import com.seriegel.tv.domain.model.Episode
import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaybackRequest(
    val contentId: String,
    val title: String,
    val streamUrl: String,
    val seriesId: String,
    val episodeId: String,
    val episodeTitle: String? = null,
    val nextEpisode: Episode? = null,
    val isMovie: Boolean = false,
)

class PlaybackCoordinator {
    private val _currentRequest = MutableStateFlow<PlaybackRequest?>(null)
    val currentRequest: StateFlow<PlaybackRequest?> = _currentRequest.asStateFlow()

    fun playEpisode(
        seriesId: String,
        episode: Episode,
        streamUrl: String,
        nextEpisode: Episode?,
    ) {
        _currentRequest.value = PlaybackRequest(
            contentId = "${seriesId}_${episode.id}",
            title = episode.title,
            streamUrl = streamUrl,
            seriesId = seriesId,
            episodeId = episode.id,
            episodeTitle = episode.title,
            nextEpisode = nextEpisode,
            isMovie = false,
        )
    }

    fun playMovie(movie: Movie, streamUrl: String) {
        _currentRequest.value = PlaybackRequest(
            contentId = "movie_${movie.id}",
            title = movie.title,
            streamUrl = streamUrl,
            seriesId = movie.id,
            episodeId = "movie",
            episodeTitle = movie.title,
            nextEpisode = null,
            isMovie = true,
        )
    }

    fun playDownloaded(item: DownloadItem) {
        _currentRequest.value = PlaybackRequest(
            contentId = item.id,
            title = item.title,
            streamUrl = item.streamUrl,
            seriesId = item.seriesId ?: item.id,
            episodeId = item.episodeId ?: "offline",
            episodeTitle = item.title,
            nextEpisode = null,
            isMovie = !item.isSeries,
        )
    }
}
