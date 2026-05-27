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
    currentSong: Song?,
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
            if (currentSong != null) {
                PlayerScreen(
                    song = currentSong,
                    onBackClick = { navController.popBackStack() },

                    // Logic to jump to the Next Song
                    onNextClick = {
                        val currentIndex = fakeSongs.indexOf(currentSong)
                        if (currentIndex != -1) {
                            // The modulo (%) operator ensures that if we are at the last song,
                            // clicking "Next" loops back to the first song (index 0).
                            val nextIndex = (currentIndex + 1) % fakeSongs.size
                            onSongSelected(fakeSongs[nextIndex])
                        }
                    },

                    // Logic to jump to the Previous Song
                    onPreviousClick = {
                        val currentIndex = fakeSongs.indexOf(currentSong)
                        if (currentIndex != -1) {
                            // Adding fakeSongs.size prevents the index from becoming a negative number
                            // when we click "Previous" on the very first song.
                            val prevIndex = (currentIndex - 1 + fakeSongs.size) % fakeSongs.size
                            onSongSelected(fakeSongs[prevIndex])
                        }
                    }
                )
            }
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }
    }
}