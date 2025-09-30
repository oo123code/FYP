package com.navigine.indoornavigationdemo.presentation.ui.screens.login

// All the necessary imports. Android Studio helps you add these with Alt+Enter.
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.data.User

@Composable
fun LoginScreen(
    // These are functions passed from the navigator to move to other screens
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isErrorDialog by remember { mutableStateOf(false) }
    var successfulUser by remember { mutableStateOf<User?>(null) }

    // --- UI Logic to show Alerts ---
    // This `LaunchedEffect` will run whenever the loginResult state changes
    LaunchedEffect(loginViewModel.loginResult) {
        val result = loginViewModel.loginResult
        if (result != null) {
            when (result) {
                is LoginResult.Success -> {
                    dialogTitle = "Login Successful"
                    dialogMessage = "Welcome back, ${result.user.firstName}!"
                    successfulUser = result.user
                    isErrorDialog = false // This was a success
                }
                is LoginResult.Error -> {
                    dialogTitle = "Login Failed"
                    dialogMessage = result.message
                    isErrorDialog = true // This was an error
                }
            }
            showDialog = true // This will now correctly trigger the AlertDialog
            loginViewModel.loginResult = null // Reset the result
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Close dialog if user taps outside
            title = { Text(text = dialogTitle) },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false // Dismiss the dialog
                        successfulUser?.let { user ->
                            onLoginSuccess(user)
                        }
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // --- UI Layout (equivalent of your JSX) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Equivalent of <ScrollView>
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Sign in to your account",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(40.dp))

        // --- Input Fields ---
        OutlinedTextField(
            value = loginViewModel.identifier,
            onValueChange = { loginViewModel.identifier = it },
            label = { Text("Email or Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = loginViewModel.password,
            onValueChange = { loginViewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (loginViewModel.isPasswordSecure) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                val icon = if (loginViewModel.isPasswordSecure) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { loginViewModel.isPasswordSecure = !loginViewModel.isPasswordSecure }) {
                    Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                }
            }
        )
        Spacer(Modifier.height(20.dp))

        // --- Role Selector ---
        RoleSelector(
            selectedRole = loginViewModel.role,
            onRoleSelected = { loginViewModel.role = it }
        )
        Spacer(Modifier.height(20.dp))

        // --- Login Button ---
        Button(
            onClick = { println("Login Button onClick fired!"); loginViewModel.onLoginClicked() },
            modifier = Modifier.fillMaxWidth(),
//            enabled = !loginViewModel.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFfb5b5a))
        ) {
            if (loginViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))

        TextButton(onClick = { /* TODO */ }) {
            Text("Forgot Password?", color = Color(0xFF007BFF))
        }
        Text("or")
        TextButton(onClick = onNavigateToRegister) {
            Text("Register account", color = Color(0xFF007BFF))
        }
    }

    // --- CHANGE START ---
    // This effect runs when the composable enters the screen and cleans up when it leaves.
    DisposableEffect(Unit) {
        onDispose {
            // This block is executed when the user navigates away from LoginScreen.
            loginViewModel.clearFields()
        }
    }
    // --- CHANGE END ---
}

@Composable
private fun RoleSelector(selectedRole: String, onRoleSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val roles = listOf("user", "management")
        roles.forEach { role ->
            val isSelected = selectedRole == role
            OutlinedButton(
                onClick = { onRoleSelected(role) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color(0xFFfb5b5a) else Color.Transparent
                ),
                border = ButtonDefaults.outlinedButtonBorder.takeIf { !isSelected }
            ) {
                Text(
                    text = role.replaceFirstChar { it.uppercase() },
                    color = if (isSelected) Color.White else Color(0xFFfb5b5a),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}