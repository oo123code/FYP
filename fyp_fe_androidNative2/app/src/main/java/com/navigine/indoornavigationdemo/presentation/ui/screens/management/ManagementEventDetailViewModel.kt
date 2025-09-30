package com.navigine.indoornavigationdemo.presentation.ui.screens.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.data.EventFeedback
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MgmtEventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val feedbackList: List<EventFeedback> = emptyList(),
    val error: String? = null
)

class ManagementEventDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MgmtEventDetailUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Fetches both the event details and the feedback list in parallel.
     */
    fun loadEventData(eventId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Use coroutineScope to run network calls concurrently for better performance.
                coroutineScope {
                    val eventDetailsDeferred = async { RetrofitInstance.api.getEventById(eventId) }
                    val feedbackListDeferred = async { RetrofitInstance.api.getFeedbackForEvent(eventId) }

                    val eventResponse = eventDetailsDeferred.await()
                    val feedbackResponse = feedbackListDeferred.await()

                    // Process the responses
                    val event = if (eventResponse.isSuccessful) eventResponse.body()?.firstOrNull() else null
                    val feedback = if (feedbackResponse.isSuccessful) feedbackResponse.body() else emptyList()

                    if (event != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                event = event,
                                feedbackList = feedback ?: emptyList()
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load event details.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred.") }
            }
        }
    }
}