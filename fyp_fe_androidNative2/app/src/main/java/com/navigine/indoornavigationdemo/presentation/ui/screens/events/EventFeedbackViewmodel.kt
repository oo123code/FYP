package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.EventFeedbackRequest
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State holder for the screen's condition
data class FeedbackUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false
)

class EventFeedbackViewmodel : ViewModel() {

    // --- State for the UI condition ---
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState = _uiState.asStateFlow()

    // --- State for the form fields ---
    // Using simple mutableStateOf for form fields is often easier in the ViewModel.
    var rating by mutableStateOf(0) // 0 means no rating given yet
        private set

    var comment by mutableStateOf("")
        private set

    // --- Public functions for the UI to call ---
    fun onRatingChange(newRating: Int) {
        rating = newRating
    }

    fun onCommentChange(newComment: String) {
        comment = newComment
    }

    /**
     * Submits the feedback to the backend.
     */
    fun submitFeedback(eventId: Int, userId: Int) {
        // Validation check: A rating is mandatory.
        if (rating == 0) {
            _uiState.update { it.copy(error = "Please select a rating before submitting.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, submissionSuccess = false) }
            try {
                val request = EventFeedbackRequest(
                    userId = userId,
                    rating = rating,
                    comment = comment
                )
                val response = RetrofitInstance.api.submitFeedback(eventId, request)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, submissionSuccess = true) }
                } else {
                    // Handle business logic errors (e.g., "already submitted")
//                    val errorMessage = response.errorBody()?.string() ?: "Failed to submit feedback."
//                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                    val errorJson = response.errorBody()?.string()
                    var errorMessage = "An unknown error occurred." // A default message
                    if (errorJson != null) {
                        try {
                            // Create a Moshi adapter to parse our simple {"message": "..."} object.
                            val moshi = Moshi.Builder().build()
                            val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
                            val adapter = moshi.adapter<Map<String, String>>(type)

                            val errorResponse = adapter.fromJson(errorJson)
                            // Extract the meaningful message.
                            errorMessage = errorResponse?.get("message") ?: "Failed to submit feedback."
                        } catch (e: Exception) {
                            // If parsing fails for some reason, we still have a fallback.
                            println("Failed to parse error JSON: $e")
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                    // --- END OF FIX ---
                }
            } catch (e: Exception) {
                // Handle network errors
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred.") }
            }
        }
    }
}