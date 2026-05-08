package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.ContinueWatchingEntry
import com.seriegel.tv.domain.model.DownloadItem
import com.seriegel.tv.domain.model.Movie
import com.seriegel.tv.domain.model.Series
import com.seriegel.tv.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val title: String = "Inicio Android TV",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val heroItems: List<HomeCard> = emptyList(),
    val sections: List<HomeSection> = emptyList(),
    val activeDownloads: List<DownloadItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
)

data class HomeSection(
    val title: String,
    val items: List<HomeCard>,
)

sealed interface HomeCard {
    val id: String
    val title: String
    val imageUrl: String

    data class SeriesCard(
        override val id: String,
        override val title: String,
        override val imageUrl: String,
        val subtitle: String?,
        val favorite: Boolean,
    ) : HomeCard

    data class MovieCard(
        override val id: String,
        override val title: String,
        override val imageUrl: String,
        val subtitle: String?,
    ) : HomeCard
}

class HomeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val catalogRepository: CatalogRepository = container.catalogRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.downloadsRepository.activeDownloads.collect { active ->
                _uiState.update { it.copy(activeDownloads = active) }
            }
        }
        viewModelScope.launch { container.downloadsRepository.refresh() }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val catalogResult = catalogRepository.refreshCatalog()
            val favoritesResult = catalogRepository.fetchFavorites()
            val continueResult = catalogRepository.fetchContinueWatching()

            val catalog = catalogResult.getOrNull()
            if (catalog == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = catalogResult.exceptionOrNull()?.message ?: "No se pudo cargar catalogo",
                    )
                }
                return@launch
            }

            val favorites = favoritesResult.getOrDefault(emptySet())
            val continueWatching = continueResult.getOrDefault(emptyList())
            val sections = buildSections(catalog.series, catalog.movies, continueWatching, favorites)
            val hero = sections.flatMap { it.items }.take(8)

            _uiState.update {
                it.copy(
                    title = catalog.title.ifBlank { "SeriesGal TV" },
                    isLoading = false,
                    errorMessage = null,
                    heroItems = hero,
                    sections = sections,
                    favorites = favorites,
                )
            }
        }
    }

    fun toggleFavorite(seriesId: String) {
        viewModelScope.launch {
            catalogRepository.toggleFavorite(seriesId)
            refresh()
        }
    }

    fun cancelDownload(downloadId: String) {
        viewModelScope.launch {
            container.downloadsRepository.remove(downloadId)
            container.downloadsRepository.refresh()
        }
    }
}

private fun buildSections(
    allSeries: List<Series>,
    allMovies: List<Movie>,
    continueWatching: List<ContinueWatchingEntry>,
    favorites: Set<String>,
): List<HomeSection> {
    val seriesCards = allSeries.map { series ->
        HomeCard.SeriesCard(
            id = series.id,
            title = series.title,
            imageUrl = ServerConfig.coverUrl(series.id),
            subtitle = series.type ?: series.lang,
            favorite = series.id in favorites,
        )
    }
    val movieCards = allMovies.map { movie ->
        HomeCard.MovieCard(
            id = movie.id,
            title = movie.title,
            imageUrl = ServerConfig.coverUrl(movie.id),
            subtitle = listOfNotNull(movie.year, movie.type).joinToString(" • ").ifBlank { null },
        )
    }

    val continueCards = continueWatching.mapNotNull { cw ->
        allSeries.firstOrNull { it.id == cw.seriesId }?.let { series ->
            HomeCard.SeriesCard(
                id = series.id,
                title = "${series.title} · ${cw.episodeTitle.ifBlank { cw.episodeId }}",
                imageUrl = ServerConfig.coverUrl(series.id),
                subtitle = "Progreso ${(cw.ratio * 100).toInt()}%",
                favorite = series.id in favorites,
            )
        }
    }

    val pending = continueWatching.filter { it.ratio in 0.05f..0.94f }.mapNotNull { cw ->
        allSeries.firstOrNull { it.id == cw.seriesId }?.let { series ->
            HomeCard.SeriesCard(
                id = series.id,
                title = series.title,
                imageUrl = ServerConfig.coverUrl(series.id),
                subtitle = "Pendiente ${(cw.ratio * 100).toInt()}%",
                favorite = series.id in favorites,
            )
        }
    }

    val recommendationSeed = continueWatching.firstOrNull()?.seriesId
    val becauseYouWatched = if (recommendationSeed != null) {
        allSeries.filter { it.id != recommendationSeed }.take(12).map { series ->
            HomeCard.SeriesCard(
                id = series.id,
                title = series.title,
                imageUrl = ServerConfig.coverUrl(series.id),
                subtitle = "Porque viste ${recommendationSeed.take(8)}",
                favorite = series.id in favorites,
            )
        }
    } else {
        emptyList()
    }

    val recentlyAdded = (allSeries.takeLast(10).map { series ->
        HomeCard.SeriesCard(
            id = series.id,
            title = series.title,
            imageUrl = ServerConfig.coverUrl(series.id),
            subtitle = "Reciente",
            favorite = series.id in favorites,
        )
    } + allMovies.takeLast(8).map { movie ->
        HomeCard.MovieCard(
            id = movie.id,
            title = movie.title,
            imageUrl = ServerConfig.coverUrl(movie.id),
            subtitle = "Reciente",
        )
    }).take(18)

    val mostViewed = (allSeries.sortedBy { it.id.hashCode() }.take(10).map { series ->
        HomeCard.SeriesCard(
            id = series.id,
            title = series.title,
            imageUrl = ServerConfig.coverUrl(series.id),
            subtitle = "Top semanal",
            favorite = series.id in favorites,
        )
    } + movieCards.take(8)).take(18)

    val sections = mutableListOf<HomeSection>()
    if (continueCards.isNotEmpty()) sections += HomeSection("Seguir viendo", continueCards)
    if (pending.isNotEmpty()) sections += HomeSection("Pendientes por terminar", pending)
    if (becauseYouWatched.isNotEmpty()) sections += HomeSection("Porque viste X", becauseYouWatched)
    if (recentlyAdded.isNotEmpty()) sections += HomeSection("Recientemente añadidos", recentlyAdded)
    if (mostViewed.isNotEmpty()) sections += HomeSection("Más vistos esta semana", mostViewed)
    if (movieCards.isNotEmpty()) sections += HomeSection("Películas", movieCards)
    if (seriesCards.isNotEmpty()) sections += HomeSection("Series", seriesCards)
    return sections
}
