package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// You will need to import your UserEventCard, EmptyState, etc.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    userId: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToEventDetails: (eventId: Int) -> Unit,
    myEventsViewModel: MyEventsViewModel = viewModel()
) {
    val uiState by myEventsViewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        if (userId != null) {
            myEventsViewModel.loadRegisteredEvents(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Registered Events") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                uiState.error != null -> Text("Error: ${uiState.error}")
                uiState.registeredEvents.isEmpty() -> {
                    // Use your EmptyState composable or a simple Text
                    Text("You have not registered for any events yet.")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.registeredEvents,
                            key = { event -> event.eventId }
                        ) { event ->
                            // Reuse the same event card for consistency!
                            UserEventCard(
                                event = event,
                                onClick = { onNavigateToEventDetails(event.eventId) }
                            )
                        }
                    }
                }
            }
        }
    }
}