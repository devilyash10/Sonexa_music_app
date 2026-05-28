package com.example.sonexa.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sonexa.feature.home.HomeScreen
import com.example.sonexa.feature.player.PlayerScreen
import com.example.sonexa.feature.search.SearchScreen
import com.example.sonexa.model.Song

@Composable
fun AppNavigation(
    navController: NavHostController,
    currentSong: Song?,
    songs: List<Song>,
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    isShuffleEnabled: Boolean,
    repeatMode: Int,

    onSongSelected: (Song) -> Unit,
    onPermissionGranted: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,

    // THE MISSING SEARCH PARAMETERS:
    searchQuery: String,
    filteredSongs: List<Song>,
    onSearchQueryChange: (String) -> Unit,

    favoriteSongIds: List<Long>,
    onToggleFavorite: (Song) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                songs = songs,
                onSongClick = { songTitle ->
                    val selectedSong = songs.find { it.title == songTitle }
                    if (selectedSong != null) {
                        onSongSelected(selectedSong)
                        navController.navigate(Screen.Player.route)
                    }
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onPermissionGranted = onPermissionGranted,

                // NEW: Connect the Home Screen button directly to the ViewModel's new function!
                onShufflePlayClick = onShuffleClick
                // (Since onShuffleClick is already passed to AppNavigation, we can just reuse it,
                // OR better yet, let's just make sure your MainScreen passes homeViewModel.shuffleAndPlayAll() here).
            )
        }

        composable(Screen.Player.route) {
            if (currentSong != null) {
                PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    isShuffleEnabled = isShuffleEnabled,
                    repeatMode = repeatMode,
                    // We instantly check if the current song's ID exists in the Room database!
                    isFavorite = favoriteSongIds.contains(currentSong.id),
                    onToggleFavorite = { onToggleFavorite(currentSong) },

                    onBackClick = { navController.popBackStack() },
                    onPauseClick = onPauseClick,
                    onResumeClick = onResumeClick,
                    onSeek = onSeek,
                    onShuffleClick = onShuffleClick,
                    onRepeatClick = onRepeatClick,
                    onNextClick = onNextClick,
                    onPreviousClick = onPreviousClick
                )
            }
        }

        composable(Screen.Search.route) {
            // Passing the search data directly into the Search UI!
            SearchScreen(
                searchQuery = searchQuery,
                filteredSongs = filteredSongs,
                onSearchQueryChange = onSearchQueryChange,
                onSongClick = { selectedSong ->
                    onSongSelected(selectedSong)
                    navController.navigate(Screen.Player.route)
                }
            )
        }
    }
}