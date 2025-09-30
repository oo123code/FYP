package com.navigine.indoornavigationdemo.presentation.ui.screens.profile

import androidx.compose.runtime.mutableStateOf
import com.navigine.indoornavigationdemo.data.User
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.UpdateUserRequest
import kotlinx.coroutines.launch


// A sealed class to represent one-time events from the ViewModel to the UI
sealed class ProfileEvent {
    data class Success(val message: String) : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
    object AccountDeleted : ProfileEvent() // A special event for deletion
}

class ProfileViewModel : ViewModel() {

    // --- STATE ---
    // Holds the user data displayed on the screen
    var user by mutableStateOf<User?>(null)
        private set

    // Holds the values being edited in the TextFields
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // UI state
    var isLoading by mutableStateOf(false)
    var event by mutableStateOf<ProfileEvent?>(null) // For sending one-time events like alerts

    // This function is called by the screen to give the ViewModel its initial data
    fun initializeUser(loggedInUser: User) {
        user = loggedInUser
        // Initialize the editable fields with the user's current data
        username = loggedInUser.username
        email = loggedInUser.email
    }

    // --- LOGIC ---
    fun onUpdateClicked() {
        val userId = user?.userId?.toInt() ?: return // Don't proceed if user is null
        isLoading = true

        viewModelScope.launch {
            // --- CHANGE START ---
            // Logic to decide if the password should be included in the request.
            val passwordToSend: String? = if (password.isNotBlank()) {
                println("New password is not blank, will be included in update request.")
                password
            } else {
                println("New password field is blank, sending null to backend (it will be ignored).")
                null // If the field is empty, send null.
            }

            val requestBody = UpdateUserRequest(
                username = username,
                email = email,
                password = passwordToSend // Use our prepared nullable password.
            )
            // --- CHANGE END ---

            // ... execute your Retrofit call with this requestBody ...
        }
    }

    fun onDeleteClicked() {
        val userId = user?.userId ?: return
        isLoading = true

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteUser(userId)
                if (response.isSuccessful) {
                    event = ProfileEvent.AccountDeleted
                } else {
                    throw Exception("Failed to delete account.")
                }
            } catch (e: Exception) {
                event = ProfileEvent.Error(e.message ?: "An unknown error occurred.")
            } finally {
                isLoading = false
            }
        }
    }
}