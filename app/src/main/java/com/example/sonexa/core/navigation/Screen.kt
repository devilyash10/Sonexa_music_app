package com.example.sonexa.core.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object Player : Screen("player")

    object Search : Screen("search")

}