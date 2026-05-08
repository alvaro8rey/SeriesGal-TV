package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.Episode
import com.seriegel.tv.domain.model.Series
import com.seriegel.tv.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpisodeUi(
    val episode: Episode,
    val progressRatio: Float,
    val isCompleted: Boolean,
)

data class ContinueWatchingUi(
    val episodeId: String,
    val episodeTitle: String,
    val progressPercent: Int,
)

data class SeriesDetailUiState(
    val isLoading: Boolean = true,
    val series: Series? = null,
    val selectedSeasonIndex: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val favorite: Boolean = false,
    val continueWatching: ContinueWatchingUi? = null,
    val navigateToPlayer: Boolean = false,
    val episodes: List<EpisodeUi> = emptyList(),
    val errorMessage: String? = null,
)

class SeriesDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val catalogRepository: CatalogRepository = container.catalogRepository
    private val playbackCoordinator = container.playbackCoordinator
    private var completedEpisodeIds: Set<String> = emptySet()

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

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
                    navigateToPlayer = false,
                )
            }
            completedEpisodeIds = catalogRepository.fetchSeriesProgress(series.id).getOrDefault(emptySet())
            val continueWatching = fetchContinueWatchingForSeries(series)
            _uiState.update { it.copy(continueWatching = continueWatching) }
            rebuildEpisodes(series, seasonIndex = 0, page = 0)
        }
    }

    fun selectSeason(index: Int) {
        _uiState.value.series?.let { series ->
            rebuildEpisodes(series, seasonIndex = index, page = 0)
        }
    }

    fun previousPage() {
        val state = _uiState.value
        val target = (state.currentPage - 1).coerceAtLeast(0)
        if (target == state.currentPage) return
        state.series?.let { series ->
            rebuildEpisodes(series, state.selectedSeasonIndex, target)
        }
    }

    fun nextPage() {
        val state = _uiState.value
        val target = (state.currentPage + 1).coerceAtMost(state.totalPages - 1)
        if (target == state.currentPage) return
        state.series?.let { series ->
            rebuildEpisodes(series, state.selectedSeasonIndex, target)
        }
    }

    fun toggleFavorite() {
        val series = _uiState.value.series ?: return
        viewModelScope.launch {
            catalogRepository.toggleFavorite(series.id)
            _uiState.update { it.copy(favorite = !it.favorite) }
        }
    }

    fun playEpisode(episode: Episode) {
        dispatchPlayEpisode(episode)
    }

    fun resumeContinueWatching() {
        val state = _uiState.value
        val series = state.series ?: return
        val continueEpisodeId = state.continueWatching?.episodeId ?: return
        val episode = series.allEpisodes.firstOrNull { it.id == continueEpisodeId } ?: return
        dispatchPlayEpisode(episode)
    }

    fun consumeNavigateToPlayer() {
        _uiState.update { it.copy(navigateToPlayer = false) }
    }

    fun refreshContinueWatching() {
        val series = _uiState.value.series ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(continueWatching = fetchContinueWatchingForSeries(series))
            }
        }
    }

    private fun dispatchPlayEpisode(episode: Episode) {
        val state = _uiState.value
        val series = state.series ?: return
        val allEpisodes = series.allEpisodes
        val currentIndex = allEpisodes.indexOfFirst { it.id == episode.id }
        if (currentIndex < 0) return
        val next = allEpisodes.getOrNull(currentIndex + 1)

        playbackCoordinator.playEpisode(
            seriesId = series.id,
            episode = episode,
            streamUrl = ServerConfig.streamUrl(episode.urlPath),
            nextEpisode = next,
        )
        _uiState.update {
            it.copy(
                navigateToPlayer = true,
            )
        }
    }

    private fun rebuildEpisodes(series: Series, seasonIndex: Int, page: Int) {
        viewModelScope.launch {
            val season = series.seasons.getOrNull(seasonIndex) ?: return@launch
            val pageSize = 20
            val totalPages = ((season.episodes.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            val safePage = page.coerceIn(0, totalPages - 1)
            val pageEpisodes = season.episodes.drop(safePage * pageSize).take(pageSize)

            val episodeItems = pageEpisodes.map { episode ->
                val completed = episode.id in completedEpisodeIds
                EpisodeUi(
                    episode = episode,
                    progressRatio = if (completed) 1f else 0f,
                    isCompleted = completed,
                )
            }
            _uiState.update {
                it.copy(
                    selectedSeasonIndex = seasonIndex,
                    currentPage = safePage,
                    totalPages = totalPages,
                    episodes = episodeItems,
                )
            }
        }
    }

    private suspend fun fetchContinueWatchingForSeries(series: Series): ContinueWatchingUi? {
        val remote = catalogRepository.fetchContinueWatching()
            .getOrDefault(emptyList())
            .firstOrNull { entry ->
                entry.seriesId == series.id && entry.ratio in 0.01f..0.95f
            }
        val chosen = remote ?: catalogRepository.fetchCachedContinueWatching(series.id)
        return chosen?.let { entry ->
            val episode = series.allEpisodes.firstOrNull { it.id == entry.episodeId } ?: return@let null
            ContinueWatchingUi(
                episodeId = episode.id,
                episodeTitle = episode.title.ifBlank { entry.episodeTitle.ifBlank { entry.episodeId } },
                progressPercent = (entry.ratio * 100).toInt().coerceIn(1, 99),
            )
        }
    }

    override fun onCleared() = super.onCleared()
}
