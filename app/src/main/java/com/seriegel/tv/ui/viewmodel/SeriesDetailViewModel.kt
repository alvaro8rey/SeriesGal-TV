package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.DownloadQuality
import com.seriegel.tv.domain.model.Episode
import com.seriegel.tv.domain.model.Series
import com.seriegel.tv.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpisodeUi(
    val episode: Episode,
    val progressRatio: Float,
    val isCompleted: Boolean,
    val download: DownloadItem?,
)

data class SeriesDetailUiState(
    val isLoading: Boolean = true,
    val series: Series? = null,
    val selectedSeasonIndex: Int = 0,
    val favorite: Boolean = false,
    val episodes: List<EpisodeUi> = emptyList(),
    val qualitySelection: DownloadQuality = DownloadQuality.HIGH,
    val errorMessage: String? = null,
)

class SeriesDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val catalogRepository: CatalogRepository = container.catalogRepository
    private val downloadsRepository = container.downloadsRepository
    private val playbackCoordinator = container.playbackCoordinator

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadsRepository.downloads.collect {
                _uiState.value.series?.let { series ->
                    rebuildEpisodes(series, _uiState.value.selectedSeasonIndex)
                }
            }
        }
    }

    fun load(seriesId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val series = catalogRepository.getSeries(seriesId)
            if (series == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Serie no encontrada") }
                return@launch
            }
            val favorite = catalogRepository.fetchFavorites().getOrDefault(emptySet()).contains(seriesId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    series = series,
                    favorite = favorite,
                    selectedSeasonIndex = 0,
                )
            }
            rebuildEpisodes(series, 0)
        }
    }

    fun selectSeason(index: Int) {
        _uiState.value.series?.let { series ->
            rebuildEpisodes(series, index)
            _uiState.update { it.copy(selectedSeasonIndex = index) }
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

    fun toggleFavorite() {
        val series = _uiState.value.series ?: return
        viewModelScope.launch {
            catalogRepository.toggleFavorite(series.id)
            _uiState.update { it.copy(favorite = !it.favorite) }
        }
    }

    fun playEpisode(episode: Episode) {
        val state = _uiState.value
        val series = state.series ?: return
        val currentSeason = series.seasons.getOrNull(state.selectedSeasonIndex) ?: return
        val index = currentSeason.episodes.indexOfFirst { it.id == episode.id }
        val next = currentSeason.episodes.getOrNull(index + 1)
        playbackCoordinator.playEpisode(
            seriesId = series.id,
            episode = episode,
            streamUrl = ServerConfig.streamUrl(episode.urlPath),
            nextEpisode = next,
        )
    }

    fun toggleEpisodeDownload(episode: Episode) {
        val series = _uiState.value.series ?: return
        val existing = _uiState.value.episodes.firstOrNull { it.episode.id == episode.id }?.download
        viewModelScope.launch {
            if (existing != null) {
                downloadsRepository.remove(existing.id)
            } else {
                downloadsRepository.enqueue(
                    id = "${series.id}_${episode.id}",
                    title = "${series.title} - ${episode.title}",
                    streamUrl = ServerConfig.streamUrl(episode.urlPath),
                    quality = _uiState.value.qualitySelection,
                    isSeries = true,
                    seriesId = series.id,
                    episodeId = episode.id,
                )
            }
            rebuildEpisodes(series, _uiState.value.selectedSeasonIndex)
        }
    }

    private fun rebuildEpisodes(series: Series, seasonIndex: Int) {
        viewModelScope.launch {
            val season = series.seasons.getOrNull(seasonIndex) ?: return@launch
            val remoteCompleted = catalogRepository.fetchSeriesProgress(series.id).getOrDefault(emptySet())
            val downloadItems = downloadsRepository.downloads.first()
            val episodeItems = season.episodes.map { episode ->
                val progressResult = catalogRepository.fetchEpisodeProgress(series.id, episode.id).getOrNull()
                val progressRatio = progressResult?.ratio ?: 0f
                EpisodeUi(
                    episode = episode,
                    progressRatio = progressRatio,
                    isCompleted = episode.id in remoteCompleted || progressRatio > 0.95f,
                    download = downloadItems.firstOrNull {
                        it.seriesId == series.id && it.episodeId == episode.id
                    },
                )
            }
            _uiState.update { it.copy(episodes = episodeItems) }
        }
    }
}
