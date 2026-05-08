package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
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
    val sections: List<HomeSection> = emptyList(),
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
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val catalogResult = catalogRepository.refreshCatalog()
            val favoritesResult = catalogRepository.fetchFavorites()

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
            val sections = buildSections(catalog.series, catalog.movies, favorites)

            _uiState.update {
                it.copy(
                    title = catalog.title.ifBlank { "SeriesGal TV" },
                    isLoading = false,
                    errorMessage = null,
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
}

private fun buildSections(
    allSeries: List<Series>,
    allMovies: List<Movie>,
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

    val sections = mutableListOf<HomeSection>()
    if (seriesCards.isNotEmpty()) sections += HomeSection("Series", seriesCards)
    if (movieCards.isNotEmpty()) sections += HomeSection("Películas", movieCards)
    return sections
}
