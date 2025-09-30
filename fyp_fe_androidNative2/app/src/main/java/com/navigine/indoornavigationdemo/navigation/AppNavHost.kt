package com.navigine.indoornavigationdemo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.navigine.indoornavigationdemo.presentation.ui.screens.HomeScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.login.LoginScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.SettingsScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.registration.RegistrationScreen
import com.navigine.indoornavigationdemo.presentation.locations.LocationsScreen
import com.navigine.indoornavigationdemo.presentation.locations.LocationsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.navigine.indoornavigationdemo.presentation.ui.main.MainViewModel
import com.navigine.indoornavigationdemo.presentation.ui.screens.events.EventsScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.profile.ProfileScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.profile.UpdateProfileScreen

@Composable
fun AppNavHost(
    mainNavController: NavHostController, // The master controller
    tabNavController: NavHostController,  // The tab controller
//    onNavigateToRegister: () -> Unit,      // The function to call
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel, // Receives the shared ViewModel
//    onLoginSuccessNavigation: () -> Unit
    onNavigateToPreferences: () -> Unit = { mainNavController.navigate("preferences") }
) {
    val currentUser by mainViewModel.currentUser.collectAsState()

    NavHost(
        navController = tabNavController, // This NavHost is for the TABS
        startDestination = NavBar.Home.route,
        modifier = modifier
    ) {
        composable(NavBar.Home.route) {
            EventsScreen(
                mainViewModel = mainViewModel, // <-- PASS THE mainViewModel HERE
                onNavigateToEventDetails = { eventId ->
                    mainNavController.navigate("event_detail/$eventId")
                }
            )
        }
        composable(NavBar.Map.route) {
            val vm: LocationsViewModel = viewModel()
            LocationsScreen(viewModel = vm)
        }

//        composable("login_in_tab") {
//            LoginScreen(
//                onLoginSuccess = { user ->
//                    println("LoginViewModel reported success for user: ${user.username}")
//                    mainViewModel.onLoginSuccess(user)
//                    // --- CHANGE START ---
//                    // Instead of navigating here, we call the lambda passed from MainScreen.
////                    onLoginSuccessNavigation()
//                    // --- CHANGE END ---
//                },
//                onNavigateToRegister = {
//                    mainNavController.navigate("register")
//                }
//            )
//        }

        composable(NavBar.Profile.route) {
            if (currentUser != null) {
                // If logged IN, show the profile.
                ProfileScreen(
                    user = currentUser!!,
                    onLogout = { mainViewModel.onLogout() }, // ViewModel just clears state
                    onNavigateToManagementDashboard = { mainNavController.navigate("management_dashboard") },
                    onNavigateToPreferences = { mainNavController.navigate("preferences") },
                    onAccountDeleted = {
                        mainViewModel.deleteAccount() // Assume deleteAccount also handles logout
                        tabNavController.navigate(NavBar.Home.route) {
                            popUpTo(tabNavController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToUpdate = {
                        tabNavController.navigate("update_profile")
                    },
                    onNavigateToMyEvents = { mainNavController.navigate("my_events") }
                )
            } else {
                // If logged OUT, show the login screen.
                LoginScreen(
                    onLoginSuccess = { user -> mainViewModel.onLoginSuccess(user) },
                    onNavigateToRegister = { tabNavController.navigate("register_in_tab") },
                    loginViewModel = viewModel()
                )
            }
        }
        composable("update_profile") {
            UpdateProfileScreen(
                mainViewModel = mainViewModel,
                onUpdateSuccess = {
                    println("Update successful, navigating back to profile.")
                    tabNavController.popBackStack() // Go back to the profile screen
                },
                onNavigateBack = {
                    println("Back button clicked, navigating back to profile.")
                    tabNavController.popBackStack()
                }
            )
        }

        composable("register_in_tab") {
            RegistrationScreen(
                onRegistrationSuccess = { tabNavController.popBackStack() }, // Go back to login
                onNavigateToLogin = { tabNavController.popBackStack() }
            )
        }
    }
}