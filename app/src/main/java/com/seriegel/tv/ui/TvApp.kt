package com.seriegel.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seriegel.tv.TvApplication
import com.seriegel.tv.ui.navigation.TvRoutes
import com.seriegel.tv.ui.screens.AuthScreen
import com.seriegel.tv.ui.screens.DownloadsScreen
import com.seriegel.tv.ui.screens.HomeScreen
import com.seriegel.tv.ui.screens.MovieDetailScreen
import com.seriegel.tv.ui.screens.PlayerScreen
import com.seriegel.tv.ui.screens.ProfileScreen
import com.seriegel.tv.ui.screens.SeriesDetailScreen
import com.seriegel.tv.ui.screens.SplashScreen
import com.seriegel.tv.ui.viewmodel.AppSessionViewModel
import com.seriegel.tv.ui.viewmodel.SessionUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun TvApp() {
    val navController = rememberNavController()
    val sessionViewModel: AppSessionViewModel = viewModel()
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext as TvApplication
    val appContainer = context.appContainer
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionUiState.Loading -> navController.navigate(TvRoutes.SPLASH) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }

            SessionUiState.LoggedOut -> navController.navigate(TvRoutes.AUTH) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }

            is SessionUiState.LoggedIn -> navController.navigate(TvRoutes.HOME) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = TvRoutes.SPLASH,
    ) {
        composable(TvRoutes.SPLASH) {
            SplashScreen()
        }
        composable(TvRoutes.AUTH) {
            AuthScreen()
        }
        composable(TvRoutes.HOME) {
            HomeScreen(
                onOpenSeries = { navController.navigate(TvRoutes.seriesDetail(it)) },
                onOpenMovie = { navController.navigate(TvRoutes.movieDetail(it)) },
                onOpenProfile = { navController.navigate(TvRoutes.PROFILE) },
            )
        }
        composable(TvRoutes.PROFILE) {
            ProfileScreen(
                onOpenDownloads = { navController.navigate(TvRoutes.DOWNLOADS) },
                onLogout = {
                    sessionViewModel.logout()
                    navController.navigate(TvRoutes.AUTH) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(TvRoutes.DOWNLOADS) {
            DownloadsScreen(
                onBack = { navController.popBackStack() },
                onPlayDownload = { downloadId ->
                    scope.launch {
                        val item = appContainer.downloadsRepository.downloads.first().firstOrNull { it.id == downloadId }
                        if (item != null) {
                            appContainer.playbackCoordinator.playDownloaded(item)
                            navController.navigate(TvRoutes.PLAYER)
                        }
                    }
                },
            )
        }
        composable(
            route = TvRoutes.SERIES_DETAIL,
            arguments = listOf(navArgument("seriesId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId").orEmpty()
            SeriesDetailScreen(
                seriesId = seriesId,
                onPlay = { navController.navigate(TvRoutes.PLAYER) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = TvRoutes.MOVIE_DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() },
                onPlay = { navController.navigate(TvRoutes.PLAYER) },
            )
        }
        composable(TvRoutes.PLAYER) {
            PlayerScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
