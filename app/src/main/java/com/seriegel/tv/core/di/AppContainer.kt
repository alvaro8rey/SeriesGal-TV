package com.seriegel.tv.core.di

import android.content.Context
import com.seriegel.tv.data.local.db.AppDatabase
import com.seriegel.tv.data.local.preferences.SessionDataStore
import com.seriegel.tv.data.remote.BackendApiService
import com.seriegel.tv.data.remote.CatalogApiService
import com.seriegel.tv.data.repository.CatalogRepositoryImpl
import com.seriegel.tv.data.repository.DownloadsRepositoryImpl
import com.seriegel.tv.data.repository.AuthRepositoryImpl
import com.seriegel.tv.domain.repository.AuthRepository
import com.seriegel.tv.domain.repository.CatalogRepository
import com.seriegel.tv.domain.repository.DownloadsRepository
import com.seriegel.tv.player.PlayerFactory
import com.seriegel.tv.player.PlaybackCoordinator
import com.seriegel.tv.core.network.NetworkProvider

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val backendApiService: BackendApiService = NetworkProvider.createApi(BackendApiService::class.java)
    private val catalogApiService: CatalogApiService = NetworkProvider.createWeb(CatalogApiService::class.java)

    val sessionDataStore: SessionDataStore = SessionDataStore(appContext)

    val database: AppDatabase = AppDatabase.create(appContext)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        apiService = backendApiService,
        sessionDataStore = sessionDataStore,
    )

    val catalogRepository: CatalogRepository = CatalogRepositoryImpl(
        backendApi = backendApiService,
        catalogApi = catalogApiService,
        sessionDataStore = sessionDataStore,
        progressDao = database.progressDao(),
    )

    val downloadsRepository: DownloadsRepository = DownloadsRepositoryImpl(
        context = appContext,
        downloadDao = database.downloadDao(),
    )

    val playerFactory: PlayerFactory = PlayerFactory(appContext)

    val playbackCoordinator: PlaybackCoordinator = PlaybackCoordinator()
}
