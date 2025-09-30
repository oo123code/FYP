package com.navigine.indoornavigationdemo.presentation.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.LoginRequest
import com.navigine.indoornavigationdemo.data.LoginResponse
import com.navigine.indoornavigationdemo.data.User
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel : ViewModel() {

    var identifier by mutableStateOf("")
    var password by mutableStateOf("")
    var role by mutableStateOf("user")
    var isPasswordSecure by mutableStateOf(true)
    var isLoading by mutableStateOf(false)
    var loginResult by mutableStateOf<LoginResult?>(null)

    fun clearFields() {
        println("LoginViewModel: Clearing identifier and password fields.")
        identifier = ""
        password = ""
    }

    fun onLoginClicked() {
        if (identifier.isBlank() || password.isBlank()) {
            loginResult = LoginResult.Error("Please fill in all fields.")
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = LoginRequest(identifier, password, role)
                println("Sending this to the backend: $requestBody")

                val response = withTimeout(10000) {
                    RetrofitInstance.api.login(requestBody)
                }

                if (response.isSuccessful) {
                    // SUCCESS PATH (HTTP 200 OK)
                    val successBodyString = response.body()?.string()
                    if (successBodyString.isNullOrBlank()) {
                        throw Exception("Login failed: Server sent an empty success response.")
                    }
                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(LoginResponse::class.java)
                    val loginResponseData = adapter.fromJson(successBodyString)!!
                    loginResult = LoginResult.Success(loginResponseData.user)

                } else {
                    // ERROR PATH (HTTP 401, 404, 500, etc.)
                    val errorBodyString = response.errorBody()?.string()
                    var errorMessage = "An unknown server error occurred."
                    if (!errorBodyString.isNullOrBlank()) {
                        try {
                            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                            val adapter = moshi.adapter(com.navigine.indoornavigationdemo.data.ErrorResponse::class.java)
                            val errorResponse = adapter.fromJson(errorBodyString)
                            errorMessage = errorResponse?.message ?: "Invalid credentials."
                        } catch (e: Exception) {
                            errorMessage = errorBodyString // Fallback to raw text
                        }
                    }
                    throw Exception(errorMessage)
                }

            } catch (e: TimeoutException) {
                loginResult = LoginResult.Error("Server took too long to respond.")
            } catch (e: Exception) {
                loginResult = LoginResult.Error(e.message ?: "An unexpected error occurred.")
            } finally {
                isLoading = false
            }
        }
    }
}