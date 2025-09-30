package com.navigine.indoornavigationdemo.presentation.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.presentation.ui.main.MainViewModel

// UpdateProfileScreen.kt (NEW FILE)

//@Composable
//fun UpdateProfileScreen(
//    mainViewModel: MainViewModel, // To get the current user data
//    updateViewModel: UpdateProfileViewModel = viewModel(),
//    onUpdateSuccess: () -> Unit
//) {
//    val currentUser = mainViewModel.currentUser.collectAsState().value
//
//    // Load user data into the form's viewmodel when the screen first composes
//    LaunchedEffect(currentUser) {
//        currentUser?.let {
//            updateViewModel.loadInitialData(it)
//        }
//    }
//
//    // --- SOLUTION FOR ISSUE 4 ---
//    // Clear the ViewModel state when the user navigates away
//    DisposableEffect(Unit) {
//        onDispose {
//            updateViewModel.clearFields()
//        }
//    }
//
//    // Your UI for the update form here.
//    // Use fields from `updateViewModel`, like `updateViewModel.firstName`.
//    // The main button should call `updateViewModel.onUpdateClicked()`.
//
//    // Example:
//    Column {
//        TextField(value = updateViewModel.username, onValueChange = { updateViewModel.username = it })
//        TextField(value = updateViewModel.email, onValueChange = { updateViewModel.email = it })
//        TextField(value = updateViewModel.newPassword, onValueChange = { updateViewModel.newPassword = it })
//        Button(
//            onClick = {
//                updateViewModel.onUpdateClicked()
//                // Observe updateViewModel.updateResult to know when to call onUpdateSuccess
//            },
//            enabled = !updateViewModel.isLoading
//        ) {
//            Text("Save Changes")
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    mainViewModel: MainViewModel,
    updateViewModel: UpdateProfileViewModel = viewModel(),
    onUpdateSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser = mainViewModel.currentUser.collectAsState().value

    // --- State for the result dialog ---
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isErrorDialog by remember { mutableStateOf(true) }

    // --- Load user data into the form's viewmodel once ---
//    LaunchedEffect(currentUser) {
//        if (currentUser != null) {
//            updateViewModel.loadInitialData(currentUser)
//        }
//    }

    // --- Observe the result from the ViewModel to show the dialog ---
    LaunchedEffect(updateViewModel.updateResult) {
        val result = updateViewModel.updateResult
        if (result != null) {
            when (result) {
                is UpdateProfileViewModel.UpdateResult.Success -> {
                    dialogTitle = "Success"
                    dialogMessage = result.message
                    isErrorDialog = false
                }
                is UpdateProfileViewModel.UpdateResult.Error -> {
                    dialogTitle = "Update Failed"
                    dialogMessage = result.message
                    isErrorDialog = true
                }
            }
            showDialog = true
            updateViewModel.updateResult = null // Reset the result
        }
    }

    // --- The Dialog Composable ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle) },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    // --- This is the key routing logic ---
                    // If the update was successful, navigate back.
                    if (!isErrorDialog) {
                        onUpdateSuccess()
                    }
                }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back to view profile") },
                navigationIcon = {
                    // --- The back button to cancel editing ---
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to profile")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Update Your Details", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Spacer(Modifier.height(10.dp))
            Text("Only fill in the fields you wish to change", fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.height(30.dp))

            // --- REMOVED First Name, Last Name, and Role ---

            // --- UPDATABLE Fields ---
            OutlinedTextField(
                value = updateViewModel.username,
                onValueChange = { updateViewModel.username = it },
                label = { Text("New Username") },
                // Use the placeholder to show the current username as a hint
                placeholder = { Text("Current: ${currentUser?.username ?: ""}") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = updateViewModel.email,
                onValueChange = { updateViewModel.email = it },
                label = { Text("New Email") },
                // Use the placeholder to show the current email as a hint
                placeholder = { Text("Current: ${currentUser?.email ?: ""}") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(15.dp))

            // New Password
            OutlinedTextField(
                value = updateViewModel.password,
                onValueChange = { updateViewModel.password = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (updateViewModel.isPasswordSecure) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { updateViewModel.isPasswordSecure = !updateViewModel.isPasswordSecure }) {
                        Icon(if (updateViewModel.isPasswordSecure) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle password visibility")
                    }
                }
            )
            Spacer(Modifier.height(15.dp))

            // Confirm New Password
            OutlinedTextField(
                value = updateViewModel.confirmPassword,
                onValueChange = { updateViewModel.confirmPassword = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (updateViewModel.isConfirmPasswordSecure) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { updateViewModel.isConfirmPasswordSecure = !updateViewModel.isConfirmPasswordSecure }) {
                        Icon(if (updateViewModel.isConfirmPasswordSecure) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle password visibility")
                    }
                }
            )

            Spacer(Modifier.height(30.dp))

            // Save Changes Button
            Button(
                onClick = {
                    if (currentUser != null) {
                        updateViewModel.onUpdateClicked(
                            currentUser = currentUser,
                            onGlobalUserUpdate = { updatedUser ->
                                mainViewModel.onLoginSuccess(updatedUser)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !updateViewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFfb5b5a))
            ) {
                // --- SOLUTION FOR ISSUE 1 ---
                // This is the loading indicator logic from your registration button.
                if (updateViewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Changes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                // --- END SOLUTION ---
            }
        }
    }
}