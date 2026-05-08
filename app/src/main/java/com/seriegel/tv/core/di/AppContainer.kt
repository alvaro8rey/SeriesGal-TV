package com.seriegel.tv.core.di

import android.content.Context
import com.seriegel.tv.data.local.db.AppDatabase
import com.seriegel.tv.data.local.preferences.SessionDataStore
import com.seriegel.tv.data.remote.AuthApiService
import com.seriegel.tv.data.repository.AuthRepositoryImpl
import com.seriegel.tv.domain.repository.AuthRepository
import com.seriegel.tv.downloads.DownloadsStateStore
import com.seriegel.tv.player.PlayerFactory
import com.seriegel.tv.core.network.NetworkProvider

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val authApiService: AuthApiService = NetworkProvider.create(AuthApiService::class.java)

    val sessionDataStore: SessionDataStore = SessionDataStore(appContext)

    val database: AppDatabase = AppDatabase.create(appContext)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        apiService = authApiService,
        sessionDataStore = sessionDataStore,
    )

    val playerFactory: PlayerFactory = PlayerFactory(appContext)

    val downloadsStateStore: DownloadsStateStore = DownloadsStateStore()
}
