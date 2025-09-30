package com.navigine.indoornavigationdemo.presentation.ui.screens.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    registrationViewModel: RegistrationViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isErrorDialog by remember { mutableStateOf(true) }

    LaunchedEffect(registrationViewModel.registrationResult) {
        val result = registrationViewModel.registrationResult
        if (result != null) {
            when (result) {
                is RegistrationResult.Success -> {
                    dialogTitle = "Success"
                    dialogMessage = result.message
                    isErrorDialog = false
                }
                is RegistrationResult.Error -> {
                    dialogTitle = "Registration Failed"
                    dialogMessage = result.message
                    isErrorDialog = true
                }
            }
            showDialog = true
            registrationViewModel.registrationResult = null
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle) },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (!isErrorDialog) {
                        onRegistrationSuccess()
                    }
                }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Spacer(Modifier.height(10.dp))
        Text("Get started with your new account", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(30.dp))

        // --- Input Fields ---
        OutlinedTextField(value = registrationViewModel.firstName, onValueChange = { registrationViewModel.firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(value = registrationViewModel.lastName, onValueChange = { registrationViewModel.lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(value = registrationViewModel.username, onValueChange = { registrationViewModel.username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(value = registrationViewModel.email, onValueChange = { registrationViewModel.email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(15.dp))

        // Password
        OutlinedTextField(
            value = registrationViewModel.password,
            onValueChange = { registrationViewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (registrationViewModel.isPasswordSecure) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { registrationViewModel.isPasswordSecure = !registrationViewModel.isPasswordSecure }) {
                    Icon(if (registrationViewModel.isPasswordSecure) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle password visibility")
                }
            }
        )
        Spacer(Modifier.height(15.dp))

        // Confirm Password
        OutlinedTextField(
            value = registrationViewModel.confirmPassword,
            onValueChange = { registrationViewModel.confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (registrationViewModel.isConfirmPasswordSecure) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { registrationViewModel.isConfirmPasswordSecure = !registrationViewModel.isConfirmPasswordSecure }) {
                    Icon(if (registrationViewModel.isConfirmPasswordSecure) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle password visibility")
                }
            }
        )
        Spacer(Modifier.height(20.dp))

        // Role Selector
        RoleSelector(
            selectedRole = registrationViewModel.role,
            onRoleSelected = {
                registrationViewModel.role = it
                println("Role selected: $it") // Console log for tracing
            }
        )
        Spacer(Modifier.height(20.dp))

        // Register Button
        Button(
            onClick = { registrationViewModel.onRegisterClicked() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !registrationViewModel.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFfb5b5a))
        ) {
            if (registrationViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Register", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))

        // Login Link
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", color = Color.Gray)
            TextButton(onClick = onNavigateToLogin) {
                Text("Login", color = Color(0xFF007BFF), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Reusable RoleSelector composable, translated from your JSX
@Composable
private fun RoleSelector(selectedRole: String, onRoleSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        listOf("user", "management").forEach { role ->
            val isSelected = selectedRole == role
            OutlinedButton(
                onClick = { onRoleSelected(role) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color(0xFFfb5b5a) else Color.Transparent
                )
            ) {
                Text(
                    role.replaceFirstChar { it.uppercase() },
                    color = if (isSelected) Color.White else Color(0xFFfb5b5a),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}