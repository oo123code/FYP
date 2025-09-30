package com.navigine.indoornavigationdemo.presentation.ui.screens.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import com.navigine.indoornavigationdemo.data.ErrorResponse
import com.navigine.indoornavigationdemo.data.RegisterResponse
import com.navigine.indoornavigationdemo.data.RegisterUserRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException

class RegistrationViewModel : ViewModel() {

    // --- State for all input fields ---
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var role by mutableStateOf("user")

    // --- UI State ---
    var isLoading by mutableStateOf(false)
    var isPasswordSecure by mutableStateOf(true)
    var isConfirmPasswordSecure by mutableStateOf(true)
    var registrationResult by mutableStateOf<RegistrationResult?>(null)

    // --- CHANGE START: IMPLEMENTATION OF parseError ---
    /**
     * Tries to parse a JSON error string into a clean, readable message.
     * If parsing fails, it returns a generic error message.
     */
    private fun parseError(errorBodyString: String?): String {
        // Log the raw error string we received
        println("Parsing error body: '$errorBodyString'")

        if (errorBodyString.isNullOrBlank()) {
            return "An unknown error occurred."
        }

        return try {
            // 1. Create a Moshi instance to parse the error.
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(ErrorResponse::class.java)

            // 2. Attempt to parse the JSON string into our ErrorResponse data class.
            val errorResponse = adapter.fromJson(errorBodyString)

            // 3. Return the message from the parsed object. Prioritize 'message' over 'error'.
            errorResponse?.message ?: errorResponse?.error ?: "Failed to parse error message."
        } catch (e: Exception) {
            // If the errorBodyString is not valid JSON, Moshi will throw an exception.
            println("Failed to parse JSON error: ${e.message}")
            // In this case, we can return the raw string if it's not too long,
            // or a generic message. A generic message is often safer.
            "An error occurred on the server."
        }
    }
    // --- CHANGE END ---

    fun onRegisterClicked() {
        // --- Frontend Validation (translated from your JS) ---
        if (firstName.isBlank() || lastName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            registrationResult = RegistrationResult.Error("Please fill in all fields.")
            return
        }
        if (password != confirmPassword) {
            registrationResult = RegistrationResult.Error("Passwords do not match.")
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody =
                    RegisterUserRequest(firstName, lastName, username, email, password, role)
                println("Sending registration request: $requestBody")

                val response = withTimeout(10000) {
                    RetrofitInstance.api.register(requestBody)
                }

                // --- CHANGE START ---
                // We check the success status code directly, INSTEAD of blindly parsing the body.
                // A 201 Created is a common successful response for registration. 200 OK is also possible.
                if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {
                    val responseBodyString = response.body()?.string()
                    println("Registration successful with HTTP code: ${response.code()}. Server response: '$responseBodyString'")

                    // We no longer try to parse the body with Moshi. We just declare it a success.
                    registrationResult = RegistrationResult.Success("Registration successful! Please log in.")

                } else {
                    // This block will now handle both network errors (4xx, 5xx) and unexpected success codes.
                    val errorBodyString = response.errorBody()?.string()
                    println("Registration failed. HTTP code: ${response.code()}. Error body: '$errorBodyString'")
                    val errorMessage = parseError(errorBodyString) // Assume you have a helper to parse error JSON //I don't have it
                    throw Exception(errorMessage) as Throwable
                }
                // --- CHANGE END ---

            } catch (e: TimeoutException) {
                registrationResult = RegistrationResult.Error("Server took too long to respond.")
            } catch (e: Exception) {
                registrationResult = RegistrationResult.Error(e.message ?: "An unexpected error occurred")
            } finally {
                isLoading = false
            }
        }
    }
}

// Sealed class to represent the result
sealed class RegistrationResult {
    data class Success(val message: String) : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}