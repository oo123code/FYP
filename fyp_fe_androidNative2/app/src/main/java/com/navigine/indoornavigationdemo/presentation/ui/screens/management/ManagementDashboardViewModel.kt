package com.navigine.indoornavigationdemo.presentation.ui.screens.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val error: String? = null
)

class ManagementDashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatus = _selectedStatus.asStateFlow()

    val filteredEvents: StateFlow<List<Event>> =
        combine(_searchQuery, _selectedStatus, _uiState) { query, statuses, state ->
            val eventsToFilter = state.events

            val keywordFiltered = if (query.isBlank()) {
                eventsToFilter
            } else {
                eventsToFilter.filter { event ->
                    event.eventName?.contains(query, ignoreCase = true) == true ||
                            event.eventLocation?.contains(query, ignoreCase = true) == true
                }
            }

            if (statuses.isEmpty()) {
                keywordFiltered
            } else {
                keywordFiltered.filter { event -> event.eventStatus in statuses }
            }
        }.stateIn( // This converts the result into a StateFlow the UI can collect.
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Standard configuration
            initialValue = emptyList() // Start with an empty list
        )

    init {
        loadEvents()
    }

    // --- 3. ADD A FUNCTION TO UPDATE THE QUERY ---
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: String) {
        val currentStatuses = _selectedStatus.value.toMutableSet()
        if (status in currentStatuses) {
            currentStatuses.remove(status)
        } else {
            currentStatuses.add(status)
        }
        _selectedStatus.value = currentStatuses
    }

    fun clearStatusFilter() {
        _selectedStatus.value = emptySet()
    }

    /**
     * Fetches the list of all events from the API and updates the UI state.
     */
    fun loadEvents() {
        println("DashboardViewModel: Starting to load events...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val startTime = System.currentTimeMillis()
            try {
                val response = RetrofitInstance.api.getAllEvents()

                if (response.isSuccessful && response.body() != null) {
                    println("DashboardViewModel: Successfully loaded ${response.body()!!.size} events.")
                    _uiState.update {
                        it.copy(isLoading = false, events = response.body()!!)
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "An unknown error occurred."
                    println("DashboardViewModel: Error loading events - $errorMessage")
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
            } catch (e: Exception) {
                println("DashboardViewModel: Exception loading events - ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            } finally {
                // This block will always execute, after the try or catch.
                val duration = System.currentTimeMillis() - startTime
                val minDisplayTime = 1000L // 700 milliseconds

                if (duration < minDisplayTime) {
                    // If the network call was too fast, wait for the remaining time.
                    kotlinx.coroutines.delay(minDisplayTime - duration)
                }

                // Now, turn off the loading spinner.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- ADD THIS NEW FUNCTION ---

    /**
     * Deletes an event by its ID and reloads the event list on success.
     */
    fun deleteEvent(eventId: Int) {
        println("DashboardViewModel: Attempting to delete event with ID: $eventId")
        viewModelScope.launch {
            // We can set a general loading state. The UI can show a spinner over the whole screen
            // or on the specific item being deleted.
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = RetrofitInstance.api.deleteEvent(eventId)

                if (response.isSuccessful) {
                    println("DashboardViewModel: Successfully deleted event $eventId. Refreshing list.")
                    // --- This is the key: On success, we just reload the data. ---
                    // The loadEvents() function will handle setting isLoading to false.
                    loadEvents()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to delete event."
                    println("DashboardViewModel: Error deleting event - $errorMessage")
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }

            } catch (e: Exception) {
                println("DashboardViewModel: Exception deleting event - ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}