package com.example.sonexa.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Player : Screen("player")

    // NEW: Version 3 Placeholders
    object Library : Screen("library")
    object Online : Screen("online")
    object Settings : Screen("settings")
    // NEW: Route that carries the ID and Name of the playlist clicked
    object PlaylistDetail : Screen("playlist_detail/{playlistId}/{playlistName}") {
        fun createRoute(id: Long, name: String) =
            "playlist_detail/$id/${android.net.Uri.encode(name)}"
    }
    object Favorites : Screen("favorites")
}