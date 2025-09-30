package com.navigine.indoornavigationdemo.presentation.ui.screens.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.data.User

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    onAccountDeleted: () -> Unit,
    onNavigateToManagementDashboard: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToMyEvents: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to permanently delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        // This calls the function that will trigger the ViewModel
                        onAccountDeleted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Red for danger
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Align content to the top
    ) {
        Text("My Profile", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Spacer(Modifier.height(30.dp))
        Spacer(Modifier.height(20.dp))

        // --- User Full Name ---
        Text(
            text = "${user.firstName} ${user.lastName}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        // --- Username ---
        Text(
            text = "@${user.username}",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(30.dp))

        // --- User Details Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = user.email)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(icon = Icons.Default.Person, label = "Role", value = user.role.replaceFirstChar { it.uppercase() })
            }
        }
        Spacer(Modifier.height(30.dp))

        // --- Action Buttons ---
        OutlinedButton(
            onClick = onNavigateToUpdate,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Edit Profile", fontSize = 18.sp)
        }
        Spacer(Modifier.height(15.dp))

        if (!user.role.equals("management", ignoreCase = true)) {
            Button(
                onClick = onNavigateToMyEvents,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("My Registered Events", fontSize = 18.sp)
            }
            Spacer(Modifier.height(15.dp))
        }

        // --- CHANGE START: CONDITIONAL DASHBOARD BUTTON ---
        // This is the key. The button is only added to the composition
        // if the user's role is "management".
        if (user.role.equals("management", ignoreCase = true)) {
            Button(
                onClick = onNavigateToManagementDashboard,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                // A different color to make it stand out
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("Management Dashboard", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(15.dp))
        }
        // --- CHANGE END ---

        if (!user.role.equals("management", ignoreCase = true)) {
            Button(onClick = onNavigateToPreferences,
                modifier = Modifier.fillMaxWidth().height(50.dp),) {
                Text("My Interests / Preferences")
            }
            Spacer(Modifier.height(15.dp)) // Add a spacer after it for consistent layout
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFfb5b5a))
        ) {
            Text("Logout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(15.dp))

        OutlinedButton(
            onClick = { showDeleteDialog = true }, // This button just opens the dialog
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Delete Account", fontSize = 18.sp)
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, color = Color(0xFF333333))
        }
    }
}