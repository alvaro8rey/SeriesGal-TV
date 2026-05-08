package com.seriegel.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seriegel.tv.ui.navigation.TvRoutes
import com.seriegel.tv.ui.screens.HomeScreen
import com.seriegel.tv.ui.screens.ProfileScreen

@Composable
fun TvApp() {
    val navController = rememberNavController()
    val openCatalog: () -> Unit = remember { {} }

    NavHost(
        navController = navController,
        startDestination = TvRoutes.HOME,
    ) {
        composable(TvRoutes.HOME) {
            HomeScreen(
                onOpenCatalog = openCatalog,
                onOpenProfile = { navController.navigate(TvRoutes.PROFILE) },
            )
        }
        composable(TvRoutes.PROFILE) {
            ProfileScreen()
        }
    }
}
