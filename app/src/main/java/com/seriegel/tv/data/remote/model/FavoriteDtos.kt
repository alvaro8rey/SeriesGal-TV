@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.seriegel.tv.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class FavoriteDto(
    @JsonNames("seriesId", "series_id")
    val seriesId: String? = null,
)
