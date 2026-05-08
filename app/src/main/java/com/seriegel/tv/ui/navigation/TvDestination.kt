package com.seriegel.tv.ui.navigation

sealed interface TvDestination {
    data object Home : TvDestination
    data object Profile : TvDestination
}
