package com.seriegel.tv.data.remote

import com.seriegel.tv.data.remote.model.CatalogDto
import retrofit2.http.GET

interface CatalogApiService {
    @GET("catalog.json")
    suspend fun catalog(): CatalogDto
}
