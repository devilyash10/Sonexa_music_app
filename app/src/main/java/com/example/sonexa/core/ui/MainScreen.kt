package com.example.sonexa.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.sonexa.feature.settings.SettingsViewModel

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel = viewModel(),
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Screen.Player.route

    val songs by homeViewModel.songs.collectAsState()
    val isPlaying by homeViewModel.isPlaying.collectAsState()
    val currentPosition by homeViewModel.currentPosition.collectAsState()
    val totalDuration by homeViewModel.totalDuration.collectAsState()
    val currentSong by homeViewModel.currentSong.collectAsState()
    val isShuffleEnabled by homeViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by homeViewModel.repeatMode.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val filteredSongs by homeViewModel.filteredSongs.collectAsState()
    val favoriteSongIds by homeViewModel.favoriteSongIds.collectAsState()
    val favoriteSongs by homeViewModel.favoriteSongs.collectAsState()
    val currentLyrics by homeViewModel.currentLyrics.collectAsState()
    val activeLyricIndex by homeViewModel.activeLyricIndex.collectAsState()
    val playlists by homeViewModel.playlists.collectAsState()
    val recentlyPlayed by homeViewModel.recentlyPlayed.collectAsState()
    val mostPlayed by homeViewModel.mostPlayed.collectAsState()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            totalDuration = totalDuration,
                            onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                            onPauseClick = { homeViewModel.pauseSong() },
                            onResumeClick = { homeViewModel.resumeSong() },
                            onNextClick = { homeViewModel.skipToNext() },
                            onPreviousClick = { homeViewModel.skipToPrevious() }
                        )
                    }
                    BottomNavigationBar(navController = navController, currentRoute = currentRoute)
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
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
                searchQuery = searchQuery,
                filteredSongs = filteredSongs,
                favoriteSongIds = favoriteSongIds,
                favoriteSongs = favoriteSongs,
                playlists = playlists,
                currentLyrics = currentLyrics,
                activeLyricIndex = activeLyricIndex,
                recentlyPlayed = recentlyPlayed,
                mostPlayed = mostPlayed,
                onShufflePlayAll = { homeViewModel.shuffleAndPlayAll() },
                settingsViewModel = settingsViewModel,
                onSongSelected = { homeViewModel.playSong(it) },
                onOnlineSongSelected = { homeViewModel.playOnlineSong(it) },
                onPlayQueue = { song, queue -> homeViewModel.playFromQueue(song, queue) },
                onPermissionGranted = { homeViewModel.loadLocalAudioFiles() },
                onPauseClick = { homeViewModel.pauseSong() },
                onResumeClick = { homeViewModel.resumeSong() },
                onSeek = { homeViewModel.seekTo(it) },
                onShuffleClick = { homeViewModel.toggleShuffle() },
                onRepeatClick = { homeViewModel.cycleRepeatMode() },
                onNextClick = { homeViewModel.skipToNext() },
                onPreviousClick = { homeViewModel.skipToPrevious() },
                onSearchQueryChange = { homeViewModel.updateSearchQuery(it) },
                onToggleFavorite = { homeViewModel.toggleFavorite(it) },
                onCreatePlaylist = { homeViewModel.createPlaylist(it) },
                onAddToPlaylist = { pId, song -> homeViewModel.addSongToPlaylist(pId, song) },
                onGetPlaylistSongs = { playlistId -> homeViewModel.getPlaylistSongs(playlistId) }
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
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple(Screen.Home.route, Icons.Rounded.Home, "Home"),
            Triple(Screen.Online.route, Icons.Rounded.Explore, "Online"),
            Triple(Screen.Library.route, Icons.Rounded.LibraryMusic, "Library"),
            Triple(Screen.Settings.route, Icons.Rounded.Settings, "Settings")
        )

        items.forEach { (route, icon, description) ->
            val isSelected = currentRoute == route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                        modifier = Modifier.padding(if (isSelected) 2.dp else 0.dp)
                    )
                },
                selected = isSelected,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                ),
                onClick = {
                    navController.navigate(route) {
                        // 🚨 BUG FIX: Custom back-stack clearing for the Home button!
                        if (route == Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        } else {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

//@Composable
//private fun BottomNavigationBar(
//    navController: NavHostController,
//    currentRoute: String?
//) {
//    NavigationBar(
//        tonalElevation = 8.dp
//    ) {
//        // 1. Home
//        NavigationBarItem(
//            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
//            selected = currentRoute == Screen.Home.route,
//            alwaysShowLabel = false,
//            onClick = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false }; launchSingleTop = true } }
//        )
//        // 2. Search
//        NavigationBarItem(
//            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
//            selected = currentRoute == Screen.Search.route,
//            alwaysShowLabel = false,
//            onClick = { navController.navigate(Screen.Search.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
//        )
//        // 3. NEW: Library
//        NavigationBarItem(
//            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
//            selected = currentRoute == Screen.Library.route,
//            alwaysShowLabel = false,
//            onClick = { navController.navigate(Screen.Library.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
//        )
//        // 4. NEW: Online
//        NavigationBarItem(
//            icon = { Icon(Icons.Default.Public, contentDescription = "Online") },
//            selected = currentRoute == Screen.Online.route,
//            alwaysShowLabel = false,
//            onClick = { navController.navigate(Screen.Online.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
//        )
//    }
//}