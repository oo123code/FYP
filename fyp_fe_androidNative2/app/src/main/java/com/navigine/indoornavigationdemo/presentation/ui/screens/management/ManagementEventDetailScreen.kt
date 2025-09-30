package com.navigine.indoornavigationdemo.presentation.ui.screens.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.data.EventFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementEventDetailScreen(
    eventId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ManagementEventDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEventData(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.event?.eventName ?: "Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Dashboard")
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
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                uiState.event != null -> {
                    // We use a LazyColumn because the feedback list could be very long.
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- SECTION 1: EVENT DETAILS ---
                        item {
                            EventDetailsSection(event = uiState.event!!)
                        }

                        // --- SECTION 2: FEEDBACK HEADER ---
                        item {
                            Text(
                                "Attendee Feedback (${uiState.feedbackList.size})",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Divider()
                        }

                        // --- SECTION 3: FEEDBACK LIST ---
                        if (uiState.feedbackList.isEmpty()) {
                            item {
                                Text(
                                    "No feedback has been submitted for this event yet.",
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(uiState.feedbackList) { feedback ->
                                FeedbackListItem(feedback = feedback)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper composable to keep the main screen clean
@Composable
private fun EventDetailsSection(event: Event) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Event Information", style = MaterialTheme.typography.titleLarge)
        Divider()
        // Reuse your existing InfoRowWithIcon or similar composables here
        // InfoRowWithIcon(icon = Icons.Default.Info, label = "Description", value = event.eventDescription ?: "")
        // InfoRowWithIcon(icon = Icons.Default.CalendarToday, label = "Date & Time", value = "...")
        // ... etc.
    }
}

// A new composable for displaying a single feedback item
@Composable
private fun FeedbackListItem(feedback: EventFeedback) {
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
                // You'll need to update your EventFeedback DTO/Entity to include the User's name
                // for this to be most effective.
                Text("Anonymous User", fontWeight = FontWeight.Bold)

                // Display the star rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(feedback.rating.toString(), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFD700))
                }
            }
            Spacer(Modifier.height(8.dp))
            if (!feedback.comment.isNullOrBlank()) {
                Text(feedback.comment)
            } else {
                Text("No comment provided.", fontStyle = FontStyle.Italic, color = Color.Gray)
            }
        }
    }
}