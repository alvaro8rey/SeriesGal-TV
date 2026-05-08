package com.seriegel.tv.domain.repository

import com.seriegel.tv.domain.model.Catalog
import com.seriegel.tv.domain.model.ContinueWatchingEntry
import com.seriegel.tv.domain.model.EpisodeProgress
import com.seriegel.tv.domain.model.Movie
import com.seriegel.tv.domain.model.ProgressUpdate
import com.seriegel.tv.domain.model.Series
import kotlinx.coroutines.flow.StateFlow

interface CatalogRepository {
    val cachedCatalog: StateFlow<Catalog?>

    suspend fun refreshCatalog(): Result<Catalog>

    suspend fun getSeries(seriesId: String): Series?

    suspend fun getMovie(movieId: String): Movie?

    suspend fun fetchFavorites(): Result<Set<String>>

    suspend fun toggleFavorite(seriesId: String): Result<Unit>

    suspend fun fetchContinueWatching(): Result<List<ContinueWatchingEntry>>

    suspend fun fetchCachedContinueWatching(seriesId: String): ContinueWatchingEntry?

    suspend fun fetchEpisodeProgress(seriesId: String, episodeId: String): Result<EpisodeProgress>

    suspend fun fetchSeriesProgress(seriesId: String): Result<Set<String>>

    suspend fun saveProgress(update: ProgressUpdate): Result<Unit>
}
