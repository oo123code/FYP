package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A simple state holder for this screen
data class MyEventsUiState(
    val isLoading: Boolean = false,
    val registeredEvents: List<Event> = emptyList(),
    val error: String? = null
)

class MyEventsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyEventsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadRegisteredEvents(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.api.getRegisteredEvents(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { it.copy(isLoading = false, registeredEvents = response.body()!!) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load events.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}