package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.DownloadQuality
import com.seriegel.tv.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val download: DownloadItem? = null,
    val resumeProgress: Float = 0f,
    val qualitySelection: DownloadQuality = DownloadQuality.HIGH,
    val errorMessage: String? = null,
)

class MovieDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val catalogRepository = container.catalogRepository
    private val downloadsRepository = container.downloadsRepository
    private val playbackCoordinator = container.playbackCoordinator

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun load(movieId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val movie = catalogRepository.getMovie(movieId)
            if (movie == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Pelicula no encontrada") }
                return@launch
            }
            val progress = catalogRepository.fetchEpisodeProgress(movie.id, "movie").getOrNull()?.ratio ?: 0f
            val downloads = downloadsRepository.downloads.first()
            val download = downloads.firstOrNull { it.id == "movie_${movie.id}" }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    movie = movie,
                    resumeProgress = progress,
                    download = download,
                )
            }
        }
    }

    fun cycleQuality() {
        val next = when (_uiState.value.qualitySelection) {
            DownloadQuality.HIGH -> DownloadQuality.MEDIUM
            DownloadQuality.MEDIUM -> DownloadQuality.LOW
            DownloadQuality.LOW -> DownloadQuality.HIGH
        }
        _uiState.update { it.copy(qualitySelection = next) }
    }

    fun playMovie() {
        val movie = _uiState.value.movie ?: return
        playbackCoordinator.playMovie(movie, ServerConfig.streamUrl(movie.streamPath))
    }

    fun toggleDownload() {
        val movie = _uiState.value.movie ?: return
        viewModelScope.launch {
            val currentDownload = _uiState.value.download
            if (currentDownload != null) {
                downloadsRepository.remove(currentDownload.id)
                _uiState.update { it.copy(download = null) }
            } else {
                downloadsRepository.enqueue(
                    id = "movie_${movie.id}",
                    title = movie.title,
                    streamUrl = ServerConfig.streamUrl(movie.streamPath),
                    quality = _uiState.value.qualitySelection,
                    isSeries = false,
                    seriesId = movie.id,
                    episodeId = "movie",
                )
                val refreshed = downloadsRepository.downloads.first().firstOrNull { it.id == "movie_${movie.id}" }
                _uiState.update { it.copy(download = refreshed) }
            }
        }
    }
}
