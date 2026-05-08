package com.seriegel.tv.downloads

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

class DownloadInfrastructure private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val databaseProvider = StandaloneDatabaseProvider(appContext)
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)

    private val upstreamDataSourceFactory = DefaultDataSource.Factory(appContext, httpDataSourceFactory)
    private val cacheFolder = File(appContext.filesDir, "offline-media")
    val downloadCache: SimpleCache = SimpleCache(
        cacheFolder,
        NoOpCacheEvictor(),
        databaseProvider,
    )

    val cacheDataSourceFactory: CacheDataSource.Factory = CacheDataSource.Factory()
        .setCache(downloadCache)
        .setUpstreamDataSourceFactory(upstreamDataSourceFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    val downloadManager: DownloadManager = DownloadManager(
        appContext,
        databaseProvider,
        downloadCache,
        httpDataSourceFactory,
        Executors.newFixedThreadPool(4),
    ).apply {
        maxParallelDownloads = 3
    }

    fun usedBytes(): Long = cacheFolder.walkBottomUp().filter { it.isFile }.sumOf { it.length() }

    companion object {
        @Volatile
        private var instance: DownloadInfrastructure? = null

        fun get(context: Context): DownloadInfrastructure {
            return instance ?: synchronized(this) {
                instance ?: DownloadInfrastructure(context).also { instance = it }
            }
        }
    }
}
