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

@Composable
fun AppNavigation(
    navController: NavHostController,
    currentSong: Song, // Receive the current song
    onSongSelected: (Song) -> Unit // Callback when a new song is picked
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = { songTitle ->
                    // Find the song that was clicked
                    val selectedSong = fakeSongs.find { it.title == songTitle }
                    if (selectedSong != null) {
                        onSongSelected(selectedSong) // Update the global state
                        navController.navigate(Screen.Player.route)
                    }
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Player.route) {
            // Pass the dynamic song data to the PlayerScreen
            PlayerScreen(
                song = currentSong,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }
    }
}