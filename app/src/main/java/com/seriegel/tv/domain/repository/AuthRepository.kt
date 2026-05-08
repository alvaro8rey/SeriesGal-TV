package com.seriegel.tv.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface SessionValidationResult {
    data object Valid : SessionValidationResult
    data object Unauthorized : SessionValidationResult
    data object NetworkError : SessionValidationResult
    data class UnknownError(val message: String?) : SessionValidationResult
}

interface AuthRepository {
    val token: Flow<String?>

    suspend fun login(username: String, password: String): Result<String>

    suspend fun register(username: String, password: String): Result<String>

    suspend fun validateSession(token: String): SessionValidationResult

    suspend fun logout()
}
