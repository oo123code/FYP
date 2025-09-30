package com.navigine.indoornavigationdemo.presentation.ui.screens.eventPreference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    userId: Int?,
    onNavigateBack: () -> Unit,
    preferencesViewModel: PreferencesViewModel = viewModel()
) {
    val uiState by preferencesViewModel.uiState.collectAsState()

    // Load the user's preferences when the screen first appears.
    LaunchedEffect(userId) {
        if (userId != null) {
            preferencesViewModel.loadUserPreferences(userId)
        }
    }

    if (uiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = {
                // This is called if the user taps outside the dialog.
                preferencesViewModel.clearSuccessMessage()
                onNavigateBack() // Go back after dismissing.
            },
            title = { Text("Success") },
            text = { Text(uiState.successMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferencesViewModel.clearSuccessMessage()
                        onNavigateBack() // Go back after clicking OK.
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Interests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to profile")
                    }
                }
            )
        }
    ) { padding ->
        if (userId == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Error: User not logged in.")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.selectedPreferences.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            "Select the event categories you're interested in:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(preferencesViewModel.allCategories) { category ->
                        PreferenceItem(
                            category = category,
                            isSelected = category in uiState.selectedPreferences,
                            onToggle = { isChecked ->
                                preferencesViewModel.onPreferenceToggle(category, isChecked)
                            }
                        )
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Button(
                    onClick = { preferencesViewModel.saveUserPreferences(userId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Preferences")
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceItem(
    category: String,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onToggle
        )
        Spacer(Modifier.width(16.dp))
        Text(category, style = MaterialTheme.typography.bodyLarge)
    }
}