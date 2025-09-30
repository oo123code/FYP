package com.navigine.indoornavigationdemo.presentation.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.UpdateUserRequest
import com.navigine.indoornavigationdemo.data.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException
import com.navigine.indoornavigationdemo.data.ErrorResponse
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.withTimeout

// UpdateProfileViewModel.kt (NEW FILE)

class UpdateProfileViewModel : ViewModel() {
//    // State for the form fields
//    var username by mutableStateOf("")
//    var email by mutableStateOf("")
//    var newPassword by mutableStateOf("")
//
//    // State for loading and results
//    var isLoading by mutableStateOf(false)
//    var updateResult by mutableStateOf<Result<String>?>(null)
//
//    // Function to populate fields when the screen is entered
//    fun loadInitialData(user: User) {
//        username = user.username
//        email = user.email
//    }
//
//    // --- SOLUTION FOR ISSUE 4 ---
//    // Function to clear fields, will be called when navigating away
//    fun clearFields() {
//        println("UpdateProfileViewModel: Clearing all form fields.")
//        newPassword = ""
//        updateResult = null
//        // You might want to keep firstName, lastName, etc., or clear them too
//        // depending on desired behavior. Clearing the password and result is most important.
//    }
//
//    fun onUpdateClicked() {
//        isLoading = true
//        viewModelScope.launch {
//            try {
//                // --- SOLUTION FOR EMPTY PASSWORD ---
//                // Only include the password in the request if it's not blank.
//                val passwordToSend: String? = if (newPassword.isNotBlank()) {
//                    println("Password field is not blank. It will be updated.")
//                    newPassword
//                } else {
//                    println("Password field is blank. It will be ignored.")
//                    null // If blank, send null. Moshi will omit this from the JSON.
//                }
//
//                val requestBody = UpdateUserRequest(
//                    username = username,
//                    email = email,
//                    password = passwordToSend
//                )
//
//                // Make your actual API call here
//                // val response = RetrofitInstance.api.updateUser(userId, requestBody)
//                // if (response.isSuccessful) { ... } else { ... }
//
//                // For now, let's simulate a success
//                delay(1000) // Simulate network delay
//                updateResult = Result.success("Profile updated successfully!")
//                println("Profile update successful.")
//
//            } catch (e: Exception) {
//                println("Error updating profile: ${e.message}")
//                updateResult = Result.failure(e)
//            } finally {
//                // --- SOLUTION FOR JAMMED BUTTON ---
//                // This block will ALWAYS execute, whether the try block succeeds or fails.
//                // This guarantees the loading state is always reset.
//                println("Update operation finished. Setting isLoading to false.")
//                isLoading = false
//            }
//        }
//    }

    // --- State for UPDATABLE input fields ---
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    // --- UI State ---
    var isLoading by mutableStateOf(false)
    var isPasswordSecure by mutableStateOf(true)
    var isConfirmPasswordSecure by mutableStateOf(true)
    var updateResult by mutableStateOf<UpdateResult?>(null)

    /**
     * Populates the ViewModel's fields with the user's current data.
     * This is called when the screen is first displayed.
     */

    // --- Reused error parsing logic from RegistrationViewModel ---
    private fun parseError(errorBodyString: String?): String {
        // ... (The exact same parseError function you provided) ...
        if (errorBodyString.isNullOrBlank()) return "An unknown error occurred."
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(ErrorResponse::class.java)
            val errorResponse = adapter.fromJson(errorBodyString)
            errorResponse?.message ?: errorResponse?.error ?: "Failed to parse error message."
        } catch (e: Exception) {
            "An error occurred on the server."
        }
    }

    fun onUpdateClicked(currentUser: User, onGlobalUserUpdate: (User) -> Unit) {
        // --- Frontend Validation ---
        if (password != confirmPassword) {
            updateResult = UpdateResult.Error("Passwords do not match.")
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                // Prepare the request: password is only sent if it's not blank.
                val usernameToSend = if (username.isNotBlank()) username else null
                val emailToSend = if (email.isNotBlank()) email else null
                val passwordToSend: String? = if (password.isNotBlank()) password else null

                val requestBody = UpdateUserRequest(
                    username = usernameToSend,
                    email = emailToSend,
                    password = passwordToSend
                )
                println("Sending update request for user ${currentUser.userId}: $requestBody")

                val response = withTimeout(10000) {
                    RetrofitInstance.api.updateUser(currentUser.userId, requestBody) //Too many arguments for 'suspend fun updateUser(userId: Int): Response<User>'.
                }

                if (response.isSuccessful) {
                    val updatedUser = response.body()
                    if (updatedUser != null) {
                        println("Update successful. New data: $updatedUser")
                        // Update the user state globally in MainViewModel
                        onGlobalUserUpdate(updatedUser)
                        // Notify the UI of success to trigger the dialog
                        updateResult = UpdateResult.Success("Your profile has been updated successfully.")
                    } else {
                        throw Exception("Update succeeded but no user data was returned.")
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    println("Update failed. HTTP code: ${response.code()}. Error body: '$errorBodyString'")
                    val errorMessage = parseError(errorBodyString)
                    throw Exception(errorMessage)
                }

            } catch (e: TimeoutException) {
                updateResult = UpdateResult.Error("Server took too long to respond.")
            } catch (e: Exception) {
                updateResult = UpdateResult.Error(e.message ?: "An unexpected error occurred")
            } finally {
                isLoading = false
            }
        }
    }

    sealed class UpdateResult {
        data class Success(val message: String) : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }
}