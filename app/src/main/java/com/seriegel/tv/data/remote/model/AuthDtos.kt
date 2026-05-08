package com.seriegel.tv.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class TokenResponseDto(
    val token: String,
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String? = null,
    @SerialName("username") val username: String? = null,
)

@Serializable
data class SeriesFavoritePayloadDto(
    val seriesId: String,
)
