package com.seriegel.tv.data.remote

import com.seriegel.tv.data.remote.model.AuthRequestDto
import com.seriegel.tv.data.remote.model.FavoriteDto
import com.seriegel.tv.data.remote.model.ProgressPayloadDto
import com.seriegel.tv.data.remote.model.ProgressResponseDto
import com.seriegel.tv.data.remote.model.SeriesFavoritePayloadDto
import com.seriegel.tv.data.remote.model.TokenResponseDto
import com.seriegel.tv.data.remote.model.UserDto
import com.seriegel.tv.data.remote.model.WatchingEntryDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {
    @POST("login")
    suspend fun login(@Body request: AuthRequestDto): TokenResponseDto

    @POST("register")
    suspend fun register(@Body request: AuthRequestDto): TokenResponseDto

    @GET("me")
    suspend fun me(@Header("Authorization") bearerToken: String): UserDto

    @GET("favorites")
    suspend fun favorites(@Header("Authorization") bearerToken: String): List<FavoriteDto>

    @POST("favorites")
    suspend fun toggleFavorite(
        @Header("Authorization") bearerToken: String,
        @Body payload: SeriesFavoritePayloadDto,
    )

    @POST("progress")
    suspend fun saveProgress(
        @Header("Authorization") bearerToken: String,
        @Body payload: ProgressPayloadDto,
    )

    @GET("continue-watching")
    suspend fun continueWatching(@Header("Authorization") bearerToken: String): List<WatchingEntryDto>

    @GET("progress/{seriesId}/{episodeId}")
    suspend fun progress(
        @Header("Authorization") bearerToken: String,
        @Path("seriesId") seriesId: String,
        @Path("episodeId") episodeId: String,
    ): ProgressResponseDto

    @GET("series-progress/{seriesId}")
    suspend fun seriesProgress(
        @Header("Authorization") bearerToken: String,
        @Path("seriesId") seriesId: String,
    ): List<String>
}
