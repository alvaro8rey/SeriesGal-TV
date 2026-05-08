package com.seriegel.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrowseCatalogItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val isSeries: Boolean,
)

enum class BrowseViewMode {
    GRID,
    LIST,
}

data class BrowseCatalogUiState(
    val contentType: String = "series",
    val title: String = "Series",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val viewMode: BrowseViewMode = BrowseViewMode.GRID,
    val items: List<BrowseCatalogItem> = emptyList(),
)

class BrowseCatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogRepository = (application as TvApplication).appContainer.catalogRepository

    private val _uiState = MutableStateFlow(BrowseCatalogUiState())
    val uiState: StateFlow<BrowseCatalogUiState> = _uiState.asStateFlow()

    fun load(contentType: String) {
        val normalizedType = if (contentType == "movies") "movies" else "series"
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    contentType = normalizedType,
                    title = if (normalizedType == "movies") "Películas" else "Series",
                )
            }
            val catalog = catalogRepository.cachedCatalog.value ?: catalogRepository.refreshCatalog().getOrNull()
            if (catalog == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el catálogo",
                        items = emptyList(),
                    )
                }
                return@launch
            }

            val mapped = if (normalizedType == "movies") {
                catalog.movies.map { movie ->
                    BrowseCatalogItem(
                        id = movie.id,
                        title = movie.title,
                        subtitle = listOfNotNull(movie.year, movie.type).joinToString(" • "),
                        imageUrl = ServerConfig.coverUrl(movie.id),
                        isSeries = false,
                    )
                }
            } else {
                catalog.series.map { series ->
                    BrowseCatalogItem(
                        id = series.id,
                        title = series.title,
                        subtitle = listOfNotNull(series.lang, series.type).joinToString(" • "),
                        imageUrl = ServerConfig.coverUrl(series.id),
                        isSeries = true,
                    )
                }
            }

            _uiState.update { it.copy(isLoading = false, errorMessage = null, items = mapped) }
        }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(
                viewMode = if (it.viewMode == BrowseViewMode.GRID) BrowseViewMode.LIST else BrowseViewMode.GRID,
            )
        }
    }
}
