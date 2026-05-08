package com.seriegel.tv.ui.navigation

object TvRoutes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val DOWNLOADS = "downloads"
    const val SERIES_DETAIL = "series_detail/{seriesId}"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val PLAYER = "player"

    fun seriesDetail(seriesId: String): String = "series_detail/$seriesId"
    fun movieDetail(movieId: String): String = "movie_detail/$movieId"
}
