package com.seriegel.tv.data.repository

import com.seriegel.tv.data.local.preferences.SessionDataStore
import com.seriegel.tv.data.remote.BackendApiService
import com.seriegel.tv.data.remote.model.AuthRequestDto
import com.seriegel.tv.domain.repository.AuthRepository
import com.seriegel.tv.domain.repository.SessionValidationResult
import android.util.Log
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val apiService: BackendApiService,
    private val sessionDataStore: SessionDataStore,
) : AuthRepository {
    private val tag = "AuthRepository"

    override val token: Flow<String?> = sessionDataStore.token

    override suspend fun login(username: String, password: String): Result<String> = runCatching {
        Log.d(tag, "login() username=$username")
        val response = apiService.login(AuthRequestDto(username, password))
        sessionDataStore.saveToken(response.token)
        response.token
    }

    override suspend fun register(username: String, password: String): Result<String> = runCatching {
        Log.d(tag, "register() username=$username")
        val response = apiService.register(AuthRequestDto(username, password))
        sessionDataStore.saveToken(response.token)
        response.token
    }

    override suspend fun validateSession(token: String): SessionValidationResult {
        return try {
            apiService.me("Bearer $token")
            Log.d(tag, "validateSession(): valid")
            SessionValidationResult.Valid
        } catch (http: HttpException) {
            Log.w(tag, "validateSession(): http=${http.code()}")
            if (http.code() == 401 || http.code() == 403) {
                SessionValidationResult.Unauthorized
            } else {
                SessionValidationResult.UnknownError(http.message())
            }
        } catch (io: IOException) {
            Log.w(tag, "validateSession(): network error", io)
            SessionValidationResult.NetworkError
        } catch (throwable: Throwable) {
            Log.e(tag, "validateSession(): unknown error", throwable)
            SessionValidationResult.UnknownError(throwable.message)
        }
    }

    override suspend fun logout() {
        sessionDataStore.clearToken()
    }
}
