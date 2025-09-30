package com.navigine.indoornavigationdemo.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.navigine.indoornavigationdemo.data.User
import com.navigine.indoornavigationdemo.presentation.ui.screens.registration.RegistrationScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.SplashScreen
import com.navigine.indoornavigationdemo.presentation.ui.main.MainScreen
import com.navigine.indoornavigationdemo.presentation.ui.main.MainViewModel
import com.navigine.indoornavigationdemo.presentation.ui.screens.eventPreference.PreferencesScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.events.EventDetailScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.events.EventFeedbackScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.events.MyEventsScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.login.LoginScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.EventFormScreen //Unresolved reference 'EventFormScreen'.
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.ManagementDashboardScreen
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.ManagementEventDetailScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigator() {
    val navController = rememberNavController() // The MASTER controller
    val mainViewModel: MainViewModel = viewModel()
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("main_flow") { popUpTo("splash") { inclusive = true } }
            })
        }

        composable("main_flow") {
            MainScreen(
                mainViewModel = mainViewModel,
                mainNavController = navController
//                onNavigateToRegister = { //No value passed for parameter 'mainNavController'.
//                    navController.navigate("register")
//                }
            )
        }

        // --- CHANGE START: ADD THE MISSING LOGIN DESTINATION ---
        // This block defines what the "login" screen is for the main NavController.
        // It was missing before, which caused the crash.
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = { user ->
//                    mainViewModel.onLoginSuccess(user)
//                    // After a successful login, we just go back. The LaunchedEffect in
//                    // MainScreen will handle navigating the user to the home tab.
//                    navController.popBackStack()
//                },
//                onNavigateToRegister = {
//                    // From the login screen, we can navigate to the register screen.
//                    navController.navigate("register")
//                }
//            )
//        }
//
//        // --- THIS IS THE CORRECT LOCATION for the RegistrationScreen ---
//        composable("register") {
//            RegistrationScreen(
//                onRegistrationSuccess = { navController.popBackStack() },
//                onNavigateToLogin = { navController.popBackStack() }
//            )
//        }

        composable("management_dashboard") {
            ManagementDashboardScreen(
                // This allows the dashboard to navigate to the form screen for editing/creating
                onNavigateToEventForm = { eventId ->
                    // If eventId is null, it's a "create" action.
                    // If it has a value, it's an "edit" action.
                    val route = if (eventId != null) "event_form/$eventId" else "event_form"
                    navController.navigate(route)
                },
                navController = navController, // Pass the controller
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFeedback = { eventId ->
                    navController.navigate("management_event_detail/$eventId")
                }
            )
        }

        //update event
        composable(
            route = "event_form/{eventId}",
            arguments = listOf(navArgument("eventId") { nullable = true })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventFormScreen(
                eventId = eventId,
                mainViewModel = mainViewModel, // <-- PASS THE MAIN VIEWMODEL
                navController = navController, // Pass the controller
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // create event
        composable("event_form") {
            EventFormScreen(
                eventId = null,
                mainViewModel = mainViewModel,
                navController = navController, // Pass the controller
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("preferences") {
            // Get the current user from the main view model
            val currentUser by mainViewModel.currentUser.collectAsState()
            PreferencesScreen(
                userId = currentUser?.userId, // Pass the user's ID
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") {
                type = NavType.IntType // Use the safer IntType
            })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")

            // This check is important in case the ID is somehow missing.
            if (eventId != null) {
                EventDetailScreen(
                    eventId = eventId,
                    mainViewModel = mainViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    detailViewModel = viewModel(),
                    onNavigateToFeedback = {id, name ->
                        // URL encode the event name to safely pass it in the route.
                        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                        navController.navigate("feedback/$id/$encodedName")}
                )
            } else {
                // Optional: Show an error or navigate back if the ID is invalid.
                Text("Error: Invalid Event ID. Please go back.")
            }
        }

        composable("my_events") {
            val currentUser by mainViewModel.currentUser.collectAsState()
            MyEventsScreen(
                userId = currentUser?.userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEventDetails = { eventId -> navController.navigate("event_detail/$eventId") }
            )
        }

        composable(
            route = "feedback/{eventId}/{eventName}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.IntType },
                navArgument("eventName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            // The event name is already URL encoded, but the navigation library decodes it automatically.
            val eventName = backStackEntry.arguments?.getString("eventName") ?: "Event"
            val currentUser by mainViewModel.currentUser.collectAsState()

            EventFeedbackScreen(
                eventId = eventId,
                eventName = eventName,
                userId = currentUser?.userId,
                // The onNavigateBack action is simple: just pop the back stack.
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // --- 2. ADD THE NEW DESTINATION ---
        composable(
            route = "management_event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            if (eventId != null) {
                ManagementEventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
    }
}
}