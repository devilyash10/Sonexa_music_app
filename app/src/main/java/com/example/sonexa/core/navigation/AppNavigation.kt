package com.example.sonexa.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sonexa.data.local.PlaylistEntity
import com.example.sonexa.feature.home.HomeScreen
import com.example.sonexa.feature.library.LibraryScreen
import com.example.sonexa.feature.library.PlaylistDetailScreen
import com.example.sonexa.feature.online.OnlineScreen
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
    onOnlineSongSelected: (Song) -> Unit,
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

    // Add this to your AppNavigation parameters:
    onGetPlaylistSongs: (Long) -> kotlinx.coroutines.flow.Flow<List<Song>>,

    favoriteSongIds: List<Long>,
    playlists: List<PlaylistEntity>,
    onCreatePlaylist: (String) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit,
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
                onShufflePlayClick = {
                    if (songs.isNotEmpty()) {
                        val randomSong = songs.random()
                        onSongSelected(randomSong)
                        if (!isShuffleEnabled) {
                            onShuffleClick()
                        }
                    }
                },

                // 🚨 ADD THIS BLOCK: It perfectly mimics clicking the bottom bar!
                onNavigateToOnline = {
                    navController.navigate(Screen.Online.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
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
                    isFavorite = favoriteSongIds.contains(currentSong.id),
                    onToggleFavorite = { onToggleFavorite(currentSong) },
                    onBackClick = { navController.popBackStack() },
                    onPauseClick = onPauseClick,
                    onResumeClick = onResumeClick,
                    onSeek = onSeek,
                    onShuffleClick = onShuffleClick,
                    onRepeatClick = onRepeatClick,
                    onNextClick = onNextClick,
                    onPreviousClick = onPreviousClick,

                    // 2. PASS THEM DOWN TO THE PLAYER SCREEN:
                    playlists = playlists,
                    onCreatePlaylist = onCreatePlaylist,
                    onAddToPlaylist = onAddToPlaylist
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

        // 1. UPDATE THE LIBRARY SCREEN
        composable(Screen.Library.route) {
            LibraryScreen(
                playlists = playlists,
                onPlaylistClick = { playlist ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlist.playlistId, playlist.name))
                }
            )
        }

        // 2. ADD THE PLAYLIST DETAIL SCREEN
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("playlistId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("playlistName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "Unknown"

            // Collect the specific songs for this playlist directly from the ViewModel!
            val playlistSongs by onGetPlaylistSongs(playlistId).collectAsState(initial = emptyList())

            PlaylistDetailScreen(
                playlistName = playlistName,
                songs = playlistSongs,
                onSongClick = { onSongSelected(it); navController.navigate(Screen.Player.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Online.route) {
            OnlineScreen(
                onOnlineSongClick = { onlineSong ->
                    // Convert the DTO schema to your app's native data structure
                    val mappedSong = Song(
                        id = onlineSong.trackId,
                        title = onlineSong.trackName ?: "Unknown Track",
                        artist = onlineSong.artistName ?: "Unknown Artist",
                        // Apple previews use a standard direct audio streaming stream url
                        mediaUri = onlineSong.previewUrl ?: "",
                        artworkUri = onlineSong.artworkUrl?.replace("100x100bb", "500x500bb") ?: ""
                    )

                    // Route directly through the unified service stream pipeline
                    onOnlineSongSelected(mappedSong)

                    // Transition smoothly into the locked master blueprint player view
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            com.example.sonexa.feature.settings.SettingsScreen()
        }
    }
}