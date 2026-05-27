package com.example.sonexa.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sonexa.feature.home.HomeScreen
import com.example.sonexa.feature.player.PlayerScreen
import com.example.sonexa.feature.search.SearchScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = {
                    navController.navigate(Screen.Player.route)
                },
                // 4. Wire up the search route
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Player.route) {
            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

//        composable(Screen.Player.route) {
//            PlayerScreen()
//        }

        composable(Screen.Search.route) {
            SearchScreen()
        }
    }
}