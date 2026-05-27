package com.example.sonexa.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sonexa.feature.home.HomeScreen
import com.example.sonexa.feature.player.PlayerScreen
import com.example.sonexa.feature.search.SearchScreen
import com.example.sonexa.model.Song
import com.example.sonexa.model.fakeSongs
import com.example.sonexa.core.navigation.Screen

@Composable
fun AppNavigation(
    navController: NavHostController,
    currentSong: Song?, // 1. Update this to be nullable (Song?)
    onSongSelected: (Song) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = { songTitle ->
                    val selectedSong = fakeSongs.find { it.title == songTitle }
                    if (selectedSong != null) {
                        onSongSelected(selectedSong)
                        navController.navigate(Screen.Player.route)
                    }
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Player.route) {
            // 2. Safely unwrap currentSong.
            // The user can only get here if they clicked a song, so it won't be null.
            if (currentSong != null) {
                PlayerScreen(
                    song = currentSong,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }
    }
}