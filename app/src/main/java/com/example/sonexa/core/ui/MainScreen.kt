package com.example.sonexa.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            // ADD THESE TWO LINES SO THE PROGRESS BAR MOVES:
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
        Surface(modifier = Modifier.padding(innerPadding)) {
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
                onSearchQueryChange = { newText -> homeViewModel.updateSearchQuery(newText) }

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
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                // FIXED NAV BUG: This explicitly clears the backstack up to Home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            selected = currentRoute == Screen.Search.route,
            onClick = {
                navController.navigate(Screen.Search.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}