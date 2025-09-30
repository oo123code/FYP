package com.navigine.indoornavigationdemo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavBar(val route: String, val title: String, val icon: ImageVector) {

    //Formerly Screen.kt
    object Home : NavBar("home", "Home", Icons.Default.Home)
    object Map : NavBar("map", "Map", Icons.Default.Map)
//    object Settings : NavBar("settings", "Settings", Icons.Default.Settings)
    object Profile : NavBar("profile", "Profile", Icons.Default.Person)
    object Register : NavBar("register", "Register", Icons.Default.Person)
}