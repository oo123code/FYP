package com.navigine.indoornavigationdemo.presentation.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.navigine.indoornavigationdemo.navigation.AppNavHost
import com.navigine.indoornavigationdemo.navigation.NavBar

@Composable
fun MainScreen(mainNavController: NavHostController, mainViewModel: MainViewModel,
//               onNavigateToRegister: () -> Unit
) {
    val tabNavController = rememberNavController()
    val screens = listOf(NavBar.Home, NavBar.Map, NavBar.Profile)
    val currentUser by mainViewModel.currentUser.collectAsState()
    val showPopup by mainViewModel.showWelcomePopup.collectAsState()


    if (showPopup) {
        AlertDialog(
            onDismissRequest = { mainViewModel.dismissWelcomePopup() },
            title = { Text("Personalize Your Experience!") },
            text = { Text("You can choose your favorite event categories in your profile to get personalized recommendations.") },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.dismissWelcomePopup()
                        // This gives the user a handy shortcut to the preferences screen
                        mainNavController.navigate("preferences")
                    }
                ) {
                    Text("Go to Preferences")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mainViewModel.dismissWelcomePopup() }
                ) {
                    Text("Maybe Later")
                }
            }
        )
    }

    // --- CHANGE START ---
    // This LaunchedEffect will reliably handle the navigation after a successful login.
    // It's keyed to `currentUser`, meaning the code block will re-run whenever `currentUser` changes.
    LaunchedEffect(currentUser) {
        // We only want to navigate if the user object has just appeared (i.e., a login just happened).
        // This prevents this from firing on app start if the user is already logged in.
        // The check `tabNavController.currentDestination?.route != NavBar.Home.route` is an extra guard
        // to ensure we only navigate if we aren't already on the home screen.
        if (currentUser != null && tabNavController.currentDestination?.route != NavBar.Home.route) {
            println("User state changed to logged in. Navigating to Home screen.")
            tabNavController.navigate(NavBar.Home.route) {
                // Clear the tab back stack to prevent going back to the login/profile screen.
                popUpTo(tabNavController.graph.findStartDestination().id)
//                {
//                    inclusive = true
//                }
            }
        }
    }
    // --- CHANGE END ---

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
//                            // --- Case 1: The clicked tab is NOT the Profile tab ---
//                            if (screen.route != NavBar.Profile.route) {
//                                // Standard navigation for Home and Map tabs.
//                                tabNavController.navigate(screen.route) {
//                                    // This is a standard best practice for bottom navigation. It prevents
//                                    // building up a large back stack as you switch between tabs.
//                                    popUpTo(tabNavController.graph.findStartDestination().id) {
//                                        saveState = true
//                                    }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            } else {
//                                // --- Case 2: The clicked tab IS the Profile tab ---
//                                if (currentUser != null) {
//                                    // If the user IS logged in, navigate normally to the Profile tab.
//                                    println("User is logged in. Navigating to Profile tab.")
//                                    tabNavController.navigate(screen.route) {
//                                        popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
//                                        launchSingleTop = true
//                                        restoreState = true
//                                    }
//                                } else {
//                                    // If the user is NOT logged in, use the main controller to go to the top-level login screen.
//                                    println("User is not logged in. Navigating to top-level Login screen.")
//                                    mainNavController.navigate("login")
//                                }
//                            }
                            tabNavController.navigate(screen.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // This is where the actual screen content will be swapped
        AppNavHost(
            mainNavController = mainNavController,
            tabNavController = tabNavController,
//            onNavigateToRegister = onNavigateToRegister,
            mainViewModel = mainViewModel,
            modifier = Modifier.padding(innerPadding)

//            onLoginSuccessNavigation = {
//                println("Login successful, navigating to Home screen.")
//                // This command tells the tab navigator to go to the Home route.
//                tabNavController.navigate(NavBar.Home.route) {
//                    // This clears the navigation stack of the tabs, so the user
//                    // can't press "back" and go to the login or profile screen.
//                    popUpTo(tabNavController.graph.findStartDestination().id) {
//                        inclusive = true
//                    }
//                }}
        )
    }
}