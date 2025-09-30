package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.data.RegistrationRequest
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.sql.Types

// State holder for the screen's data and condition
data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val error: String? = null,
    val registrationSuccessMessage: String? = null,
    val isUserRegistered: Boolean = false,
)

class EventDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Fetches the details for a specific event from the API.
     */
//    fun loadEventDetails(eventId: Int) {
//        // Prevent reloading if we already have the data for this event
//        if (_uiState.value.event?.eventId == eventId) return
//
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, error = null) }
//            try {
//                val response = RetrofitInstance.api.getEventById(eventId)
//                if (response.isSuccessful && response.body() != null) {
//                    // The endpoint returns a list, so we take the first item.
//                    val event = response.body()!!.firstOrNull()
//                    if (event != null) {
//                        _uiState.update { it.copy(isLoading = false, event = event) }
//                    } else {
//                        _uiState.update { it.copy(isLoading = false, error = "Event with ID $eventId not found.") }
//                    }
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Failed to load event details."
//                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
//                }
//            } catch (e: Exception) {
//                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred.") }
//            }
//        }
//    }

    fun loadEventDetails(eventId: Int, userId: Int?) {
        println("--- DETAIL VM DEBUG: START loadEventDetails for eventId: $eventId, userId: $userId ---")
        if (_uiState.value.event?.eventId == eventId && !_uiState.value.isLoading) {
            println("--- DETAIL VM DEBUG: Data already loaded. Skipping.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EventDetailUiState(isLoading = true)
            try {
                // Fetch event details
                println("--- DETAIL VM DEBUG: Fetching event details...")
                val eventResponse = RetrofitInstance.api.getEventById(eventId)

                // Fetch registration status
                val statusResponse = if (userId != null) {
                    println("--- DETAIL VM DEBUG: User is logged in. Fetching registration status...")
                    RetrofitInstance.api.checkRegistrationStatus(eventId, userId)
                } else {
                    println("--- DETAIL VM DEBUG: User is a guest. Skipping registration status check.")
                    null
                }

                if (eventResponse.isSuccessful && eventResponse.body() != null) {
                    val event = eventResponse.body()!!.firstOrNull()
                    println("--- DETAIL VM DEBUG: Event details fetched successfully. Event name: ${event?.eventName}")

                    // --- THIS IS THE CRITICAL LOGIC ---
                    val rawStatusMap = statusResponse?.body()
                    println("--- DETAIL VM DEBUG: Raw status response body: $rawStatusMap")
                    val isRegistered = rawStatusMap?.get("isRegistered") ?: false
                    println("--- DETAIL VM DEBUG: PARSED isRegistered VALUE: $isRegistered ---")
                    // --- END OF CRITICAL LOGIC ---

                    if (event != null) {
                        _uiState.update { it.copy(isLoading = false, event = event, isUserRegistered = isRegistered) }
                        println("--- DETAIL VM DEBUG: State updated successfully.")
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Event not found.") }
                    }
                } else {
                    println("--- DETAIL VM DEBUG: FAILED to load event. Error: ${eventResponse.errorBody()?.string()}")
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load event.") }
                }
            } catch (e: Exception) {
                println("--- DETAIL VM DEBUG: CRASHED with exception: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun registerForEvent(eventId: Int, userId: Int) {
        viewModelScope.launch {
            // Set loading state and clear any old errors/messages.
            _uiState.update { it.copy(isLoading = true, error = null, registrationSuccessMessage = null) }
            try {
                val request = RegistrationRequest(userId = userId)
                val response = RetrofitInstance.api.registerForEvent(eventId, request)

                if (response.isSuccessful && response.body() != null) {
                    // SUCCESS! The server returns the updated event.
                    val updatedEvent = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            event = updatedEvent, // Update the UI with the new attendee count
                            registrationSuccessMessage = "Successfully registered for '${updatedEvent.eventName}'!"
                        )
                    }
                } else {
                    // --- THIS IS THE CRITICAL FIX ---
                    // The request failed, so we parse the error body.
                    var specificErrorMessage = "Registration failed." // A default message
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            // Prepare Moshi to parse our simple {"message": "..."} object
                            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                            val errorAdapter = moshi.adapter<Map<String, String>>()

                            val errorMap = errorAdapter.fromJson(errorBodyString)
                            // Extract the meaningful message from the map
                            specificErrorMessage = errorMap?.get("message") ?: specificErrorMessage
                        } catch (e: Exception) {
                            specificErrorMessage = errorBodyString
                            println("Error parsing JSON error body: $e")
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, error = specificErrorMessage) }
                    // --- END OF FIX ---
                }
            } catch (e: Exception) {
                // Network error or other exception.
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred.")
                }
            }
        }
    }

    // --- 3. ADD THE unregisterFromEvent FUNCTION ---
    fun unregisterFromEvent(eventId: Int, userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, registrationSuccessMessage = null) }
            try {
                val request = RegistrationRequest(userId = userId)
                val response = RetrofitInstance.api.unregisterFromEvent(eventId, request)
                if (response.isSuccessful && response.body() != null) {
                    val updatedEvent = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            event = updatedEvent,
                            isUserRegistered = false, // The user is now unregistered
                            registrationSuccessMessage = "Successfully unregistered from '${updatedEvent.eventName}'."
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.errorBody()?.string()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(registrationSuccessMessage = null) }
    }
}