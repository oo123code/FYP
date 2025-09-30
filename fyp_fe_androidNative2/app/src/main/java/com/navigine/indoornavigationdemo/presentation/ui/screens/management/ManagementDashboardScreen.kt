package com.navigine.indoornavigationdemo.presentation.ui.screens.management

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshIndicator //Unresolved reference 'pullrefresh'. Do I need to add the dependency to build.gradle or smtg
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.navigine.indoornavigationdemo.data.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ManagementDashboardScreen.kt (NEW FILE)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ManagementDashboardScreen(
    managementViewModel: ManagementDashboardViewModel = viewModel(),
    onNavigateToEventForm: (eventId: Int?) -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    // --- 1. ADD THE NEW NAVIGATION CALLBACK ---
    onNavigateToFeedback: (eventId: Int) -> Unit,
) {

    val uiState by managementViewModel.uiState.collectAsState()
    val searchQuery by managementViewModel.searchQuery.collectAsState()
    val filteredEvents by managementViewModel.filteredEvents.collectAsState()
    val selectedStatus by managementViewModel.selectedStatus.collectAsState()

    // --- State for Pull-to-Refresh ---
    val isRefreshing = uiState.isLoading && uiState.events.isNotEmpty()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { managementViewModel.loadEvents() }
    )

    // --- Logic for Refreshing After Save ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.savedStateHandle?.get<Boolean>("event_saved") == true) {
            println("Dashboard received event_saved signal. Refreshing events.")
            managementViewModel.loadEvents()
            navBackStackEntry?.savedStateHandle?.remove<Boolean>("event_saved")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Management Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEventForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        // --- CHANGE 1: WRAP EVERYTHING IN A COLUMN ---
        // This allows us to place the search bar above the list.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- CHANGE 2: ADD THE SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = managementViewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Search by name or location...") },
                singleLine = true
            )

            StatusFilterGroup(
                selectedStatuses = selectedStatus,
                onStatusSelected = managementViewModel::onStatusSelected,
                onClearFilter = managementViewModel::clearStatusFilter
            )

            // --- CHANGE 3: THE BOX NOW CONTAINS THE DYNAMIC CONTENT ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
                contentAlignment = Alignment.Center
            ) {
                // --- State Handling ---
                if (uiState.isLoading && uiState.events.isEmpty()) {
                    CircularProgressIndicator()
                } else if (uiState.error != null) {
                    ErrorState(
                        errorMessage = uiState.error!!,
                        onRetry = { managementViewModel.loadEvents() }
                    )
                } else if (uiState.events.isEmpty()) {
                    EmptyState()
                    // --- CHANGE 4: ADDED A CHECK FOR EMPTY SEARCH RESULTS ---
                } else if (filteredEvents.isEmpty()) {
                    Text("No events found matching your search.", color = Color.Gray)
                } else {
                    // --- The List of Events ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // --- CHANGE 5: USE `filteredEvents` INSTEAD OF `uiState.events` ---
                        items(filteredEvents) { event ->
                            EventManagementListItem(
                                event = event,
                                onEdit = { onNavigateToEventForm(event.eventId) },
                                onDelete = { managementViewModel.deleteEvent(event.eventId) },
                                onViewFeedback = { onNavigateToFeedback(event.eventId) }
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

// --- Reusable UI Components for the Dashboard Screen ---

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatusFilterGroup(
    selectedStatuses: Set<String>,
    onStatusSelected: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    val statuses = listOf("UPCOMING", "ONGOING", "ENDED", "CANCELLED")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp), // Added vertical padding
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedStatuses.isEmpty(),
            onClick = onClearFilter,
            label = { Text("All") }
        )
        statuses.forEach { status ->
            FilterChip(
                selected = status in selectedStatuses,
                onClick = { onStatusSelected(status) },
                label = { Text(status.replaceFirstChar { it.titlecase() }) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
//private
fun EventManagementListItem(
    event: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    // --- 3. ACCEPT THE NEW ACTION IN THE LIST ITEM ---
    onViewFeedback: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to permanently delete '${event.eventName}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete() // This calls the ViewModel's delete function
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(event.eventName ?: "No Name",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                //ID
                Text("Event ID: ${event.eventId}",
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(Modifier.width(8.dp))
                EventStatusChip(status = event.eventStatus)
            }
            Spacer(Modifier.height(8.dp))

            Text("Date: ${formatDisplayDate(event.eventStartDateTime)}")
            Text("Time: ${formatDisplayTime(event.eventStartDateTime)}")
            Text("Venue: ${event.eventLocation ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
            Text("Attendees: ${event.eventAttendees ?: 0} / ${event.eventMaxPax ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)

            Text("Created by user with ID: ${event.createdByUserId}", fontSize = 14.sp, color = Color.Gray)
            Text("Created at: ${formatDisplayDate(event.createdAt)}", fontSize = 14.sp, color = Color.Gray)
            Text("Last edited by user with ID: ${event.editedByUserId}", fontSize = 14.sp, color = Color.Gray)
            Text("Last edited at: ${formatDisplayDate(event.editedAt)}", fontSize = 14.sp, color = Color.Gray)
            Text("Event category: ${event.eventCategory}", fontSize = 14.sp, color = Color.Gray)
//            EventCategoryChip(category = event.eventCategory)
//            Spacer(Modifier.width(8.dp))
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Event")
                }

                // --- THIS IS THE NEW BUTTON ---
                OutlinedButton(
                    onClick = onViewFeedback,
                    modifier = Modifier.weight(1f),
                    // The button is only enabled for events that have ended.
                    enabled = event.eventStatus.equals("ENDED", ignoreCase = true)
                ) {
                    Text("Feedback")
                }
                // --- END OF NEW BUTTON ---
                IconButton(onClick = { showDeleteDialog = true }) { // Open the dialog
                    Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
//private
fun EventStatusChip(status: String?) {
    val (backgroundColor, textColor) = when (status) {
        "UPCOMING" -> Color(0xFFE3F2FD) to Color(0xFF1565C0) // Light Blue / Dark Blue
        "ONGOING" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // Light Green / Dark Green
        "ENDED" -> Color(0xFFF5F5F5) to Color(0xFF616161)   // Light Grey / Dark Grey
        "CANCELLED" -> Color(0xFFFFEBEE) to Color(0xFFC62828) // Light Red / Dark Red
        else -> Color.LightGray to Color.Black
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status ?: "UNKNOWN",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
//private
fun EmptyState() {
    Text("No events found.\nTap the '+' button to create one!", color = Color.Gray)
}

@Composable
private fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDisplayDate(dateTimeString: String?): String {
    if (dateTimeString == null) return "N/A"
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString) // Parses "2025-10-20T09:00:00"
        dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: Exception) { dateTimeString }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDisplayTime(dateTimeString: String?): String {
    if (dateTimeString == null) return "N/A"
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) { "" }
}

@Composable
private fun EventCategoryChip(category: String?) {
    // This is a guard clause. If there's no category, we don't show anything.
    if (category.isNullOrBlank()) {
        return
    }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer, // A subtle, theme-aware color
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = category,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}