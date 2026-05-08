package com.seriegel.tv.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val token: Flow<String?>

    suspend fun login(username: String, password: String): Result<String>

    suspend fun register(username: String, password: String): Result<String>

    suspend fun validateSession(token: String): Result<Unit>

    suspend fun logout()
}
