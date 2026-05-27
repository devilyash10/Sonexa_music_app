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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sonexa.core.navigation.AppNavigation
import com.example.sonexa.core.navigation.Screen
import com.example.sonexa.feature.player.MiniPlayer

// ... (Keep your other imports)
import com.example.sonexa.model.Song // Make sure you import Song, NOT fakeSongs anymore

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Player.route

    // 1. Make the state nullable (Song?) and default to null
    var currentSong by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // 2. Only show the MiniPlayer if currentSong is NOT null
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
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
                onSongSelected = { newSong ->
                    currentSong = newSong
                }
            )
        }
    }
}

// ... (Keep BottomNavigationBar exactly as it is)

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    // Removed the forced .height(72.dp) so the items don't overflow!
    NavigationBar(
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
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