package com.seriegel.tv.data.repository

import android.util.Log
import com.seriegel.tv.data.local.db.dao.ProgressDao
import com.seriegel.tv.data.local.db.entity.ProgressCacheEntity
import com.seriegel.tv.data.local.preferences.SessionDataStore
import com.seriegel.tv.data.remote.BackendApiService
import com.seriegel.tv.data.remote.CatalogApiService
import com.seriegel.tv.data.remote.model.CatalogDto
import com.seriegel.tv.data.remote.model.ProgressPayloadDto
import com.seriegel.tv.data.remote.model.WatchingEntryDto
import com.seriegel.tv.domain.model.Catalog
import com.seriegel.tv.domain.model.ContinueWatchingEntry
import com.seriegel.tv.domain.model.Episode
import com.seriegel.tv.domain.model.EpisodeProgress
import com.seriegel.tv.domain.model.Movie
import com.seriegel.tv.domain.model.ProgressUpdate
import com.seriegel.tv.domain.model.Season
import com.seriegel.tv.domain.model.Series
import com.seriegel.tv.domain.repository.CatalogRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class CatalogRepositoryImpl(
    private val backendApi: BackendApiService,
    private val catalogApi: CatalogApiService,
    private val sessionDataStore: SessionDataStore,
    private val progressDao: ProgressDao,
) : CatalogRepository {
    private val tag = "CatalogRepository"
    private val _cachedCatalog = MutableStateFlow<Catalog?>(null)
    override val cachedCatalog: StateFlow<Catalog?> = _cachedCatalog

    override suspend fun refreshCatalog(): Result<Catalog> = runCatching {
        Log.d(tag, "refreshCatalog()")
        val catalog = catalogApi.catalog().toDomain()
        _cachedCatalog.value = catalog
        catalog
    }

    override suspend fun getSeries(seriesId: String): Series? {
        val catalog = _cachedCatalog.value ?: refreshCatalog().getOrNull()
        return catalog?.series?.firstOrNull { it.id == seriesId }
    }

    override suspend fun getMovie(movieId: String): Movie? {
        val catalog = _cachedCatalog.value ?: refreshCatalog().getOrNull()
        return catalog?.movies?.firstOrNull { it.id == movieId }
    }

    override suspend fun fetchFavorites(): Result<Set<String>> = runCatching {
        Log.d(tag, "fetchFavorites()")
        withToken { token ->
            backendApi.favorites(token).mapNotNull { it.seriesId }.toSet()
        }
    }

    override suspend fun toggleFavorite(seriesId: String): Result<Unit> = runCatching {
        Log.d(tag, "toggleFavorite() seriesId=$seriesId")
        withToken { token ->
            backendApi.toggleFavorite(token, com.seriegel.tv.data.remote.model.SeriesFavoritePayloadDto(seriesId))
            Unit
        }
    }

    override suspend fun fetchContinueWatching(): Result<List<ContinueWatchingEntry>> = runCatching {
        Log.d(tag, "fetchContinueWatching()")
        withToken { token ->
            backendApi.continueWatching(token).mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun fetchCachedContinueWatching(seriesId: String): ContinueWatchingEntry? {
        val latest = progressDao.latestForSeries(seriesId) ?: return null
        return ContinueWatchingEntry(
            seriesId = latest.seriesId,
            episodeId = latest.episodeId,
            episodeTitle = latest.episodeId,
            urlPath = null,
            timeSeconds = latest.timeSeconds,
            durationSeconds = latest.durationSeconds,
        )
    }

    override suspend fun fetchEpisodeProgress(seriesId: String, episodeId: String): Result<EpisodeProgress> = runCatching {
        Log.d(tag, "fetchEpisodeProgress() seriesId=$seriesId episodeId=$episodeId")
        try {
            withToken { token ->
                val response = backendApi.progress(token, seriesId, episodeId)
                EpisodeProgress(
                    timeSeconds = response.time ?: 0.0,
                    durationSeconds = response.duration ?: 0.0,
                ).also { progress ->
                    progressDao.upsert(
                        ProgressCacheEntity(
                            id = "$seriesId::$episodeId",
                            seriesId = seriesId,
                            episodeId = episodeId,
                            timeSeconds = progress.timeSeconds,
                            durationSeconds = progress.durationSeconds,
                        ),
                    )
                }
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            val cached = progressDao.get(seriesId, episodeId)
            if (cached != null) {
                EpisodeProgress(cached.timeSeconds, cached.durationSeconds)
            } else {
                throw throwable
            }
        }
    }

    override suspend fun fetchSeriesProgress(seriesId: String): Result<Set<String>> = runCatching {
        withToken { token -> backendApi.seriesProgress(token, seriesId).toSet() }
    }

    override suspend fun saveProgress(update: ProgressUpdate): Result<Unit> = runCatching {
        Log.d(tag, "saveProgress() ${update.seriesId}/${update.episodeId}")
        progressDao.upsert(
            ProgressCacheEntity(
                id = "${update.seriesId}::${update.episodeId}",
                seriesId = update.seriesId,
                episodeId = update.episodeId,
                timeSeconds = update.timeSeconds,
                durationSeconds = update.durationSeconds,
            ),
        )
        runCatching {
            withToken { token ->
                backendApi.saveProgress(
                    token,
                    ProgressPayloadDto(
                        seriesId = update.seriesId,
                        episodeId = update.episodeId,
                        time = update.timeSeconds,
                        duration = update.durationSeconds,
                        episodeTitle = update.episodeTitle,
                        url = update.urlPath,
                    ),
                )
            }
        }.onFailure { throwable ->
            Log.w(tag, "saveProgress() remote failed, kept local cache", throwable)
        }
    }

    private suspend fun <T> withToken(call: suspend (String) -> T): T {
        val token = sessionDataStore.token.first().orEmpty()
        require(token.isNotBlank()) { "No active token" }
        return call("Bearer $token")
    }
}

private fun CatalogDto.toDomain(): Catalog {
    val mappedSeries = series.map { seriesDto ->
        val seasons = when {
            !seriesDto.seasons.isNullOrEmpty() -> {
                seriesDto.seasons.map { season ->
                    Season(
                        seasonNumber = season.season,
                        title = season.title,
                        episodes = season.episodes.map { episode ->
                            Episode(
                                id = episode.id,
                                title = episode.title,
                                urlPath = episode.url,
                            )
                        },
                    )
                }
            }

            !seriesDto.episodes.isNullOrEmpty() -> {
                listOf(
                    Season(
                        seasonNumber = 1,
                        title = "Temporada 1",
                        episodes = seriesDto.episodes.map { episode ->
                            Episode(
                                id = episode.id,
                                title = episode.title,
                                urlPath = episode.url,
                            )
                        },
                    ),
                )
            }

            else -> emptyList()
        }

        Series(
            id = seriesDto.id,
            title = seriesDto.title,
            lang = seriesDto.lang,
            type = seriesDto.type,
            description = seriesDto.description,
            seasons = seasons,
        )
    }

    val mappedMovies = movies.map { movie ->
        Movie(
            id = movie.id,
            title = movie.title,
            lang = movie.lang,
            streamPath = movie.url,
            type = movie.type,
            description = movie.description,
            year = movie.year,
        )
    }

    return Catalog(
        title = title,
        series = mappedSeries,
        movies = mappedMovies,
    )
}

private fun WatchingEntryDto.toDomainOrNull(): ContinueWatchingEntry? {
    val normalizedSeriesId = seriesId ?: return null
    val normalizedEpisodeId = episodeId ?: return null
    return ContinueWatchingEntry(
        seriesId = normalizedSeriesId,
        episodeId = normalizedEpisodeId,
        episodeTitle = episodeTitle.orEmpty(),
        urlPath = url,
        timeSeconds = time ?: 0.0,
        durationSeconds = duration ?: 0.0,
    )
}
