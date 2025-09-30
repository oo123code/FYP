package com.navigine.indoornavigationdemo.presentation.ui.main

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.navigine.indoornavigationdemo.App
import com.navigine.indoornavigationdemo.network.RetrofitInstance

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // --- NEW ---
    // This state tells the UI when the initial session check is complete.
    private val _isSessionLoaded = MutableStateFlow(false)

    // Get a reference to SharedPreferences
    private val sharedPreferences = application.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Moshi for converting User object to and from JSON string
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    var isDeleting by mutableStateOf(false)
    var deleteResult by mutableStateOf<Result<String>?>(null)

    // This is the private, mutable "switch" that only the ViewModel can change.
    private val _showWelcomePopup = MutableStateFlow(false)

    // This is the public, read-only version that the UI can look at.
    val showWelcomePopup = _showWelcomePopup.asStateFlow()

    init {
        // When the ViewModel is created, check if a user session is saved
        viewModelScope.launch {
            try {
                val userJson = sharedPreferences.getString("current_user", null)
                if (userJson != null) {
                    // Add a log to see if it's working
                    println("User found in SharedPreferences, attempting to parse.")
                    _currentUser.value = userAdapter.fromJson(userJson)
                } else {
                    println("No user found in SharedPreferences.")
                }
            } catch (e: Exception) {
                // It's crucial to log parsing errors
                println("Failed to parse user from JSON: ${e.message}")
                _currentUser.value = null // Ensure user is null on error
            } finally {
                // --- NEW ---
                // No matter what happens (success, failure, no user),
                // we are now done with the initial loading.
                _isSessionLoaded.value = true
            }
        }
    }

    fun onLoginSuccess(user: User) {
        _currentUser.value = user
        println("MainViewModel: Set currentUser with role=${user.role} and ID=${user.userId}")
        // Save the user to SharedPreference
        if (!App.preferenceManager.hasCompletedOnboarding()) {
            _showWelcomePopup.value = true // If so, set the flag to show the popup.
            App.preferenceManager.setHasCompletedOnboarding(true) // Mark it as done.
        }
        viewModelScope.launch {
            val userJson = userAdapter.toJson(user)
            sharedPreferences.edit { putString("current_user", userJson) }
        }
    }

    fun onLogout() {
        _currentUser.value = null
        // Clear the user from SharedPreferences
        viewModelScope.launch {
            sharedPreferences.edit { remove("current_user") }
        }
    }

    fun deleteAccount() {
        // Guard clause: Do nothing if there's no user to delete.
        val userToDelete = _currentUser.value ?: return

        isDeleting = true
        viewModelScope.launch {
            try {
                println("Attempting to delete user with ID: ${userToDelete.userId}")
                val response = RetrofitInstance.api.deleteUser(userToDelete.userId)

                if (response.isSuccessful) {
                    println("Account deletion successful on server.")
                    // --- REUSE LOGOUT LOGIC ---
                    // After successful deletion, the user's session is invalid.
                    // Calling onLogout() will clear the SharedPreferences and the currentUser state.
                    onLogout()
                    deleteResult = Result.success("Account deleted successfully.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Account deletion failed. Code: ${response.code()}, Body: $errorBody")
                    // You can use a parse
                    // Error helper here if you have one.
                    throw Exception(errorBody ?: "Failed to delete account.")
                }
            } catch (e: Exception) {
                deleteResult = Result.failure(e)
            } finally {
                isDeleting = false
            }
        }
    }

    fun clearDeleteResult() {
        deleteResult = null
    }

    fun dismissWelcomePopup() {
        _showWelcomePopup.value = false
    }
}