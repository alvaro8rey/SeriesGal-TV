package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.Movie
import com.seriegel.tv.domain.model.Series
import java.text.Normalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
)

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val seriesResults: List<SearchResultItem> = emptyList(),
    val movieResults: List<SearchResultItem> = emptyList(),
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val catalogRepository = container.catalogRepository

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allSeries: List<Series> = emptyList()
    private var allMovies: List<Movie> = emptyList()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val catalog = catalogRepository.cachedCatalog.value ?: catalogRepository.refreshCatalog().getOrNull()
            if (catalog == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el catálogo para buscar",
                    )
                }
                return@launch
            }
            allSeries = catalog.series
            allMovies = catalog.movies
            applyFilter("")
        }
    }

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value) }
        applyFilter(value)
    }

    private fun applyFilter(query: String) {
        val cleanQuery = query.trim()
        val normalizedQuery = cleanQuery.normalizeForSearch()
        val filteredSeries = if (cleanQuery.isBlank()) {
            allSeries.take(40)
        } else {
            allSeries.filter { series ->
                series.title.normalizeForSearch().contains(normalizedQuery)
            }.take(80)
        }
        val filteredMovies = if (cleanQuery.isBlank()) {
            allMovies.take(40)
        } else {
            allMovies.filter { movie ->
                movie.title.normalizeForSearch().contains(normalizedQuery)
            }.take(80)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                seriesResults = filteredSeries.map { series ->
                    SearchResultItem(
                        id = series.id,
                        title = series.title,
                        subtitle = listOfNotNull(series.lang, series.type).joinToString(" • "),
                        imageUrl = ServerConfig.coverUrl(series.id),
                    )
                },
                movieResults = filteredMovies.map { movie ->
                    SearchResultItem(
                        id = movie.id,
                        title = movie.title,
                        subtitle = listOfNotNull(movie.year, movie.type).joinToString(" • "),
                        imageUrl = ServerConfig.coverUrl(movie.id),
                    )
                },
            )
        }
    }
}

private fun String.normalizeForSearch(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val withoutDiacritics = normalized.replace(Regex("\\p{M}+"), "")
    return withoutDiacritics.lowercase()
}
