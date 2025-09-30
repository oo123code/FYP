package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.presentation.ui.main.MainViewModel
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.EmptyState
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.EventStatusChip
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.StatusFilterGroup
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.formatDisplayDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EventsScreen(
    mainViewModel: MainViewModel,
    onNavigateToEventDetails: (eventId: Int) -> Unit,
    eventsViewModel: EventsViewModel = viewModel()
) {
    val uiState by eventsViewModel.uiState.collectAsState()
    val searchQuery by eventsViewModel.searchQuery.collectAsState()
    val filteredEvents by eventsViewModel.filteredEvents.collectAsState()
    val selectedStatuses by eventsViewModel.selectedStatuses.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()

    val isRefreshing = uiState.isLoading

    LaunchedEffect(currentUser?.userId) {
        println("EventsScreen: LaunchedEffect triggered. User role is ${currentUser?.role}")
        eventsViewModel.loadEvents(currentUser)
        if (uiState.events.isNotEmpty()) {
            println("FE DEBUG (UI): List in UI State starts with: '${uiState.events[0].eventName}'")
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            currentUser?.let { user ->
                eventsViewModel.loadEvents(currentUser)
            }
        }
    )

    val screenTitle = when {
        currentUser?.role.equals("management", ignoreCase = true) -> "All Events (Admin)"
        currentUser != null -> "Recommended For You"
        else -> "Upcoming Events"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(70.dp),
                title = { Text(screenTitle) },
                // You can add colors here to match your theme if needed
                 colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFfb5b5a), titleContentColor = Color.White)
            )
        }
    ) { padding ->

        // --- 2. WRAP IN A COLUMN ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // --- 3. ADD THE SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = eventsViewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Search events...") },
                singleLine = true
            )

            // --- 2. ADD THE STATUS FILTER CHIPS ---
            StatusFilterGroup(
                selectedStatuses = selectedStatuses,
                onStatusSelected = eventsViewModel::onStatusSelected,
                onClearFilter = eventsViewModel::clearStatusFilter
            )

            // --- 4. THE REST OF THE UI GOES INTO THE BOX ---
            Box(
                modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.error != null) {
                    ErrorState(
                        errorMessage = uiState.error!!,
                        onRetry = { eventsViewModel.loadEvents(currentUser) }
                    )
                } else if (uiState.events.isEmpty()) {
                    EmptyState()
                } else if (filteredEvents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No events found matching your search.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // --- 5. USE THE `filteredEvents` LIST ---
                        items(
                            items = filteredEvents,
                            key = { event -> event.eventId } // <-- THIS LINE IS THE ENTIRE SOLUTION
                        ) { event ->
                            UserEventCard(
                                event = event,
                                onClick = { onNavigateToEventDetails(event.eventId) }
                            )
                        }
                    }
                }
                // The visible spinner indicator for pull-to-refresh
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
/**
 * A simplified event card for regular users. It's clickable and shows only relevant info.
 */

@Composable
private fun StatusFilterGroup(
    selectedStatuses: Set<String>,
    onStatusSelected: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    // List of all statuses a user can filter by.
    val statuses = listOf("UPCOMING", "ONGOING", "ENDED", "CANCELLED")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()) // Allows scrolling if chips don't fit
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" Chip - A special case to clear the filter
        FilterChip(
            selected = selectedStatuses.isEmpty(),
            onClick = onClearFilter,
            label = { Text("All") }
        )

        // Generate a chip for each status
        statuses.forEach { status ->
            FilterChip(
                selected = status in selectedStatuses,
                onClick = { onStatusSelected(status) },
                label = { Text(status.replaceFirstChar { it.titlecase() }) } // e.g., "UPCOMING" -> "Upcoming"
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
//private
fun UserEventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // The entire card is now clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Event Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.eventName ?: "No Name",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                EventStatusChip(status = event.eventStatus)
            }
            Spacer(Modifier.height(12.dp))

            // Row 2: Date and Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Date: ", fontWeight = FontWeight.SemiBold)
                Text(formatDisplayDate(event.eventStartDateTime))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Time: ", fontWeight = FontWeight.SemiBold)
                Text(formatDisplayTime(event.eventStartDateTime))
            }
            Spacer(Modifier.height(8.dp))

            // Row 3: Venue and Attendees
            Text("Venue: ${event.eventLocation ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
            Text("Attendees: ${event.eventAttendees ?: 0} / ${event.eventMaxPax ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(12.dp))
            EventCategoryChip(category = event.eventCategory)
        }
    }
}


// --- Reusable UI Components (Copied from your ManagementDashboardScreen for consistency) ---

@Composable
//private
fun EventStatusChip(status: String?) {
    val (backgroundColor, textColor) = when (status) {
        "UPCOMING" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "ONGOING" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "ENDED" -> Color(0xFFF5F5F5) to Color(0xFF616161)
        "CANCELLED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color.LightGray to Color.Black
    }
    Surface(color = backgroundColor, shape = RoundedCornerShape(16.dp)) {
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
    Text("No events available at the moment.", color = Color.Gray)
}

@Composable
//private
fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// Helper functions for formatting (copied for self-containment)
@RequiresApi(Build.VERSION_CODES.O)
//private
fun formatDisplayDate(dateTimeString: String?): String {
    if (dateTimeString == null) return "N/A"
    return try {
        LocalDateTime.parse(dateTimeString).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: Exception) { "Invalid Date" }
}

@RequiresApi(Build.VERSION_CODES.O)
//private
fun formatDisplayTime(dateTimeString: String?): String {
    if (dateTimeString == null) return "N/A"
    return try {
        LocalDateTime.parse(dateTimeString).format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) { "Invalid Time" }
}

@Composable
fun EventCategoryChip(category: String?) {
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