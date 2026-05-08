package com.seriegel.tv.data.repository

import com.seriegel.tv.data.local.preferences.SessionDataStore
import com.seriegel.tv.data.remote.AuthApiService
import com.seriegel.tv.data.remote.model.AuthRequestDto
import com.seriegel.tv.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionDataStore: SessionDataStore,
) : AuthRepository {

    override val token: Flow<String?> = sessionDataStore.token

    override suspend fun login(username: String, password: String): Result<String> = runCatching {
        val response = apiService.login(AuthRequestDto(username, password))
        sessionDataStore.saveToken(response.token)
        response.token
    }

    override suspend fun register(username: String, password: String): Result<String> = runCatching {
        val response = apiService.register(AuthRequestDto(username, password))
        sessionDataStore.saveToken(response.token)
        response.token
    }

    override suspend fun validateSession(token: String): Result<Unit> = runCatching {
        apiService.me("Bearer $token")
        Unit
    }

    override suspend fun logout() {
        sessionDataStore.clearToken()
    }
}
