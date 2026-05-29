package com.example.sonexa.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sonexa.core.navigation.AppNavigation
import com.example.sonexa.core.navigation.Screen
import com.example.sonexa.feature.home.HomeViewModel
import com.example.sonexa.feature.player.MiniPlayer
import com.example.sonexa.model.Song
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Public

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Player.route

    // 1. Collect the real lists and states from the ViewModel
    val songs by homeViewModel.songs.collectAsState()
    val isPlaying by homeViewModel.isPlaying.collectAsState()
    val currentPosition by homeViewModel.currentPosition.collectAsState()
    val totalDuration by homeViewModel.totalDuration.collectAsState()

    val currentSong by homeViewModel.currentSong.collectAsState()
    val isShuffleEnabled by homeViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by homeViewModel.repeatMode.collectAsState()

    // REMOVED: var currentSong by remember { mutableStateOf<Song?>(null) }
    // We don't need it anymore, the ViewModel dictates the truth!
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val filteredSongs by homeViewModel.filteredSongs.collectAsState()
    val favoriteSongIds by homeViewModel.favoriteSongIds.collectAsState()
    val playlists by homeViewModel.playlists.collectAsState()
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // Removing default spaces to make it flush
                Column(modifier = Modifier.fillMaxWidth()) {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            totalDuration = totalDuration,
                            onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                            onPauseClick = { homeViewModel.pauseSong() },
                            onResumeClick = { homeViewModel.resumeSong() },
                            onNextClick = { homeViewModel.skipToNext() }
                        )
                    }
                    BottomNavigationBar(navController = navController, currentRoute = currentRoute)
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background // Explicit background color
        ) {
            AppNavigation(
                navController = navController,
                currentSong = currentSong,
                songs = songs,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                totalDuration = totalDuration,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,

                onSongSelected = { newSong ->
                    homeViewModel.playSong(newSong)
                },
                onPermissionGranted = { homeViewModel.loadLocalAudioFiles() },
                onPauseClick = { homeViewModel.pauseSong() },
                onResumeClick = { homeViewModel.resumeSong() },
                onSeek = { position -> homeViewModel.seekTo(position) },
                onShuffleClick = { homeViewModel.toggleShuffle() },
                onRepeatClick = { homeViewModel.cycleRepeatMode() },

                // ADD THESE TWO LINES: The final connection!
                onNextClick = { homeViewModel.skipToNext() },
                onPreviousClick = { homeViewModel.skipToPrevious() },
                searchQuery = searchQuery,
                filteredSongs = filteredSongs,
                onSearchQueryChange = { newText -> homeViewModel.updateSearchQuery(newText) },

                // ADD THESE TWO FOR FAVORITES:
                favoriteSongIds = favoriteSongIds,
                playlists = playlists,
                onCreatePlaylist = { playlistName -> homeViewModel.createPlaylist(playlistName) },
                onAddToPlaylist = { playlistId, songId -> homeViewModel.addSongToPlaylist(playlistId, songId) },

                onGetPlaylistSongs = { playlistId -> homeViewModel.getPlaylistSongs(playlistId) },


                onToggleFavorite = { songToLike -> homeViewModel.toggleFavorite(songToLike) }

            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        tonalElevation = 8.dp
    ) {
        // 1. Home
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = currentRoute == Screen.Home.route,
            alwaysShowLabel = false,
            onClick = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false }; launchSingleTop = true } }
        )
        // 2. Search
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            selected = currentRoute == Screen.Search.route,
            alwaysShowLabel = false,
            onClick = { navController.navigate(Screen.Search.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
        )
        // 3. NEW: Library
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
            selected = currentRoute == Screen.Library.route,
            alwaysShowLabel = false,
            onClick = { navController.navigate(Screen.Library.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
        )
        // 4. NEW: Online
        NavigationBarItem(
            icon = { Icon(Icons.Default.Public, contentDescription = "Online") },
            selected = currentRoute == Screen.Online.route,
            alwaysShowLabel = false,
            onClick = { navController.navigate(Screen.Online.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
        )
    }
}