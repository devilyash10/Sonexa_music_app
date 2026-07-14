package com.example.sonexa.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.sonexa.core.util.LyricLine
import com.example.sonexa.feature.settings.SettingsViewModel
import com.example.sonexa.feature.splash.SplashScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    currentSong: Song?,
    songs: List<Song>,

    // 🚨 1. NEW DATA ARGS: Passed down from MainScreen
    recentlyPlayed: List<Song>,
    mostPlayed: List<Song>,

    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onOnlineSongSelected: (Song) -> Unit,
    onPlayQueue: (Song, List<Song>) -> Unit,
    onSongSelected: (Song) -> Unit,
    onPermissionGranted: () -> Unit,

    // 🚨 2. NEW ACTION ARG: For the Shuffle all button
    onShufflePlayAll: () -> Unit,

    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    searchQuery: String,
    filteredSongs: List<Song>,
    onSearchQueryChange: (String) -> Unit,
    onGetPlaylistSongs: (Long) -> kotlinx.coroutines.flow.Flow<List<Song>>,
    favoriteSongIds: List<Long>,
    favoriteSongs: List<Song>,
    playlists: List<PlaylistEntity>,
    currentLyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onCreatePlaylist: (String) -> Unit,
    onAddToPlaylist: (Long, Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            // Read the policy state
            val hasAccepted by settingsViewModel.hasAcceptedPrivacyPolicy.collectAsState()

            SplashScreen(
                onSplashFinished = {
                    //  Intercept first-time users!
                    val nextDestination = if (hasAccepted) Screen.Home.route else Screen.PrivacyPolicy.route

                    navController.navigate(nextDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {

            // 🚨 3. BOTTOM SHEET STATE: Tracks which song is being added to a playlist
            var songForPlaylist by remember { mutableStateOf<Song?>(null) }

            if (songForPlaylist != null) {
                com.example.sonexa.feature.player.AddToPlaylistBottomSheet(
                    song = songForPlaylist!!,
                    playlists = playlists,
                    onDismiss = { songForPlaylist = null },
                    onCreatePlaylist = onCreatePlaylist,
                    onAddToPlaylist = { playlistId, song ->
                        onAddToPlaylist(playlistId, song)
                        songForPlaylist = null // Auto-close sheet after adding!
                    }
                )
            }

            HomeScreen(
                songs = songs,
                recentlyPlayed = recentlyPlayed,
                mostPlayed = mostPlayed,
                favoriteSongIds = favoriteSongIds,
                onSongClick = { song ->
                    // Play the song and jump to the Player Screen!
                    onPlayQueue(song, songs)
                    navController.navigate(Screen.Player.route)
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onPermissionGranted = onPermissionGranted,
                onShufflePlayClick = onShufflePlayAll, // Trigger the shuffle engine
                onNavigateToOnline = { navController.navigate(Screen.Online.route) }, // 🚨 Fixed Route
                onToggleFavorite = onToggleFavorite,
                onAddToPlaylistClick = { song ->
                    // Trigger the bottom sheet to slide up!
                    songForPlaylist = song
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
                    playlists = playlists,
                    onCreatePlaylist = onCreatePlaylist,
                    onAddToPlaylist = onAddToPlaylist,
                    currentLyrics = currentLyrics,
                    activeLyricIndex = activeLyricIndex
                )
            }
        }

        composable(Screen.Search.route) {
            SearchScreen(
                searchQuery = searchQuery,
                filteredSongs = filteredSongs,
                onSearchQueryChange = onSearchQueryChange,
                onSongClick = { selectedSong ->
                    onPlayQueue(selectedSong, filteredSongs)
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                playlists = playlists,
                onPlaylistClick = { playlist ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlist.playlistId, playlist.name))
                },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) }
            )
        }

        composable(Screen.Favorites.route) {
            com.example.sonexa.feature.library.FavoritesScreen(
                favoriteSongs = favoriteSongs,
                onSongClick = { selectedSong ->
                    onPlayQueue(selectedSong, favoriteSongs)
                    navController.navigate(Screen.Player.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("playlistId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("playlistName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "Unknown"

            val playlistSongs by onGetPlaylistSongs(playlistId).collectAsState(initial = emptyList())

            PlaylistDetailScreen(
                playlistName = playlistName,
                songs = playlistSongs,
                onSongClick = { selectedSong ->
                    onPlayQueue(selectedSong, playlistSongs)
                    navController.navigate(Screen.Player.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Online.route) {
            OnlineScreen(
                onOnlineSongClick = { onlineSong ->
                    val mappedSong = Song(
                        id = onlineSong.trackId,
                        title = onlineSong.trackName ?: "Unknown Track",
                        artist = onlineSong.artistName ?: "Unknown Artist",
                        mediaUri = onlineSong.previewUrl ?: "",
                        artworkUri = onlineSong.artworkUrl?.replace("100x100bb", "500x500bb") ?: ""
                    )
                    onOnlineSongSelected(mappedSong)
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            com.example.sonexa.feature.settings.SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToEqualizer = { navController.navigate("custom_eq") },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) }
            )
        }

        composable("custom_eq") {
            com.example.sonexa.feature.settings.CustomEqualizerScreen(
                audioSessionId = com.example.sonexa.service.AudioService.activeAudioSessionId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            val hasAccepted by settingsViewModel.hasAcceptedPrivacyPolicy.collectAsState()
            com.example.sonexa.feature.settings.PrivacyPolicyScreen(
                isFirstLaunch = !hasAccepted,
                onAcceptClick = {
                    settingsViewModel.acceptPrivacyPolicy()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PrivacyPolicy.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            com.example.sonexa.feature.settings.AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}