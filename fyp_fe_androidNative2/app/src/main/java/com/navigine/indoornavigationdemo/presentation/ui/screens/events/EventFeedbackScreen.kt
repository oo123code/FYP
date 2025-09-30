package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFeedbackScreen(
    eventId: Int,
    eventName: String,
    userId: Int?,
    onNavigateBack: () -> Unit,
    feedbackViewModel: EventFeedbackViewmodel = viewModel()
) {
    val uiState by feedbackViewModel.uiState.collectAsState()

    // Navigate back automatically on successful submission.
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leave Feedback",
                            // You might want to use a specific style
                            style = MaterialTheme.typography.titleLarge
                        )
//                        Text(
//                            text = eventName, // The event name acts as the subtitle
//                            style = MaterialTheme.typography.bodyMedium, // Use a smaller style for the subtitle
//                            color = MaterialTheme.colorScheme.onSurfaceVariant // A slightly muted color
//                        )
                    }

                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    )  { padding ->
        if (userId == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Error: You must be logged in to leave feedback.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("How was your experience?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // Star Rating Component
            StarRatingInput(
                rating = feedbackViewModel.rating,
                onRatingChange = feedbackViewModel::onRatingChange
            )
            Spacer(Modifier.height(24.dp))

            // Comment Text Field
            OutlinedTextField(
                value = feedbackViewModel.comment,
                onValueChange = feedbackViewModel::onCommentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text("Share your thoughts (optional)") },
                placeholder = { Text("What did you like or dislike?") }
            )
            Spacer(Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { feedbackViewModel.submitFeedback(eventId, userId) },
                // The button is disabled if no rating is given or if it's currently loading.
                enabled = feedbackViewModel.rating > 0 && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Feedback")
                }
            }

            // Error Message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row(horizontalArrangement = Arrangement.Center) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Star $i",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChange(i) },
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray
            )
        }
    }
}