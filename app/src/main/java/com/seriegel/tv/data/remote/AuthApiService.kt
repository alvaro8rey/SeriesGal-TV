package com.seriegel.tv.data.remote

import com.seriegel.tv.data.remote.model.AuthRequestDto
import com.seriegel.tv.data.remote.model.TokenResponseDto
import com.seriegel.tv.data.remote.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("login")
    suspend fun login(@Body request: AuthRequestDto): TokenResponseDto

    @POST("register")
    suspend fun register(@Body request: AuthRequestDto): TokenResponseDto

    @GET("me")
    suspend fun me(@Header("Authorization") bearerToken: String): UserDto
}
