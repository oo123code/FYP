package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.data.User
import com.navigine.indoornavigationdemo.presentation.ui.main.MainViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    detailViewModel: EventDetailViewModel = viewModel(),
    // --- CHANGE 1: ADD a new navigation callback for the feedback screen ---
    onNavigateToFeedback: (eventId: Int, eventName: String) -> Unit
) {
    val uiState by detailViewModel.uiState.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger the data load when the screen is first composed.
    LaunchedEffect(eventId, currentUser) {
        detailViewModel.loadEventDetails(eventId, currentUser?.userId)
    }

    LaunchedEffect(uiState.registrationSuccessMessage) {
        if (uiState.registrationSuccessMessage != null) {
            snackbarHostState.showSnackbar(uiState.registrationSuccessMessage!!)
            detailViewModel.clearSuccessMessage() // Clear the message after showing it
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                // The title is dynamic: shows a default until the event name is loaded.
                title = { Text(uiState.event?.eventName ?: "Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to events")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
                uiState.event != null -> {
                    // --- 2. Pass the user and ViewModel to the content ---
                    EventDetailsContent(
                        event = uiState.event!!,
                        currentUser = currentUser,
                        isLoading = uiState.isLoading,
                        isUserRegistered = uiState.isUserRegistered,
                        onRegisterClick = {
                            currentUser?.let { user ->
                                detailViewModel.registerForEvent(eventId, user.userId)
                            }
                        },
                        onUnregisterClick = { // <-- Add the new action
                            currentUser?.let { user ->
                                detailViewModel.unregisterFromEvent(eventId, user.userId)
                            }
                        },
                        // --- CHANGE 2: Pass the new onNavigateToFeedback callback ---
                        onLeaveFeedbackClick = {
                            // We know the event is not null here, so we can use !! safely.
                            onNavigateToFeedback(uiState.event!!.eventId, uiState.event!!.eventName ?: "Event")
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventDetailsContent(
    event: Event,
    currentUser: User?,
    isLoading: Boolean,
    onRegisterClick: () -> Unit,
    isUserRegistered: Boolean,
    onUnregisterClick: () -> Unit ,// <-- Add the new action
    onLeaveFeedbackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header Section ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventStatusChip(status = event.eventStatus)
            EventCategoryChip(category = event.eventCategory)
        }

        // --- Details Section ---
        InfoRowWithIcon(icon = Icons.Default.Info, label = "Description", value = event.eventDescription ?: "No description provided.")
        Divider()
        InfoRowWithIcon(icon = Icons.Default.CalendarToday, label = "Start Date & Time", value = "${formatDisplayDate(event.eventStartDateTime)} at ${formatDisplayTime(event.eventStartDateTime)}")
        InfoRowWithIcon(icon = Icons.Default.CalendarToday, label = "End Date & Time", value = "${formatDisplayDate(event.eventEndDateTime)} at ${formatDisplayTime(event.eventEndDateTime)}")
        Divider()
        InfoRowWithIcon(icon = Icons.Default.LocationOn, label = "Location / Venue", value = event.eventLocation ?: "N/A")
        InfoRowWithIcon(icon = Icons.Default.People, label = "Capacity", value = "${event.eventAttendees ?: 0} / ${event.eventMaxPax ?: "N/A"}")

        // --- Action Button Placeholder ---
        // This is where the "Register" button will go in the next step.
        Spacer(Modifier.height(16.dp))

        if (currentUser?.role.equals("management", ignoreCase = true)) {
            // If the user is a manager, show a simple, informative text.
            Text("You are viewing this event as a manager.", style = MaterialTheme.typography.bodyMedium)
        } else {
        when {
            // Case 1: The event has ended.
            event.eventStatus.equals("ENDED", ignoreCase = true) -> {
                // Only show the feedback button if the user was registered for this event.
                if (isUserRegistered) {
                    Button(
                        onClick = onLeaveFeedbackClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("Leave Feedback")
                    }
                } else {
                    // If they weren't registered, show a disabled button.
                    Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Event Has Ended")
                    }
                }
            }

            // Case 2: The user is currently registered for an active event.
            isUserRegistered -> {
                OutlinedButton(
                    onClick = onUnregisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Unregister")
                    }
                }
            }

            // Case 3: The user is not registered for an active event.
            else -> {
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentUser != null && !isLoading
                ){
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else if (currentUser == null) {
                        Text("Log in to Register")
                    }
                    else {
                        Text("Register for this Event")
                    }
                }
            }
        }
        }
    }
}

// A reusable composable to keep the layout clean and consistent.
@Composable
private fun InfoRowWithIcon(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = label, modifier = Modifier.padding(top = 4.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(value, fontSize = 14.sp)
        }
    }
}