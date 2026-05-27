package com.example.sonexa.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sonexa.feature.home.HomeScreen
import com.example.sonexa.feature.player.PlayerScreen
import com.example.sonexa.feature.search.SearchScreen
import com.example.sonexa.model.Song
// IMPORTANT: Remove the import for fakeSongs!

@Composable
fun AppNavigation(
    navController: NavHostController,
    currentSong: Song?,
    songs: List<Song>,

    // Accept new states
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,

    onSongSelected: (Song) -> Unit,
    onPermissionGranted: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onSeek: (Long) -> Unit // Accept seek command
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                songs = songs, // Pass the real songs to the UI
                onSongClick = { songTitle ->
                    // Search the REAL list for the clicked song
                    val selectedSong = songs.find { it.title == songTitle }
                    if (selectedSong != null) {
                        onSongSelected(selectedSong)
                        navController.navigate(Screen.Player.route)
                    }
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onPermissionGranted = onPermissionGranted // Wire up the callback
            )
        }

        composable(Screen.Player.route) {
            if (currentSong != null) {
                PlayerScreen(
                    song = currentSong,

                    // Pass the real ExoPlayer data directly into the UI
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,

                    onBackClick = { navController.popBackStack() },
                    onPauseClick = onPauseClick,
                    onResumeClick = onResumeClick,

                    // Notice how Next/Previous just call onSongSelected?
                    // Because we updated onSongSelected in MainScreen to call homeViewModel.playSong(),
                    // clicking Next will automatically play the next song!
                    onNextClick = {
                        val currentIndex = songs.indexOf(currentSong)
                        if (currentIndex != -1 && songs.isNotEmpty()) {
                            val nextIndex = (currentIndex + 1) % songs.size
                            onSongSelected(songs[nextIndex])
                        }
                    },
                    onPreviousClick = {
                        val currentIndex = songs.indexOf(currentSong)
                        if (currentIndex != -1 && songs.isNotEmpty()) {
                            val prevIndex = (currentIndex - 1 + songs.size) % songs.size
                            onSongSelected(songs[prevIndex])
                        }
                    },
                    onSeek = onSeek // Wire up the callback
                )
            }
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }
    }
}