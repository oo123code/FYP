package com.navigine.indoornavigationdemo.presentation.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.data.User
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// This data class is identical to your DashboardUiState, representing the screen's state.
data class EventsUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val error: String? = null
)

class EventsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedStatuses = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatuses = _selectedStatuses.asStateFlow()

    private var hasLoaded = false

    val filteredEvents: StateFlow<List<Event>> =
        combine(_searchQuery, _selectedStatuses, _uiState) { query, statuses, state ->

            // --- LOG 2: WHAT IS THE INPUT TO THE COMBINE BLOCK? ---
            println("FE DEBUG (Combine): Triggered. Query='${query}', Statuses=${statuses.joinToString(",")}")
            if (state.events.isNotEmpty()) {
                println("FE DEBUG (Combine): Input list starts with: '${state.events[0].eventName}'")
            } else {
                println("FE DEBUG (Combine): Input list is empty.")
            }

            // Start with the full list of events.
            val eventsToFilter = state.events

            // First, apply the text search filter.
            val keywordFiltered = if (query.isBlank()) {
                eventsToFilter // If no text, pass all events through.
            } else {
                eventsToFilter.filter { event ->
                    event.eventName?.contains(query, ignoreCase = true) == true ||
                            event.eventLocation?.contains(query, ignoreCase = true) == true
                }
            }

            val finalFilteredList = if (statuses.isEmpty()) {
                keywordFiltered
            } else {
                keywordFiltered.filter { event -> event.eventStatus in statuses }
            }

            // --- LOG 3: WHAT IS THE FINAL OUTPUT OF THE COMBINE BLOCK? ---
            if (finalFilteredList.isNotEmpty()) {
                println("FE DEBUG (Combine): Output list starts with: '${finalFilteredList[0].eventName}'")
            } else {
                println("FE DEBUG (Combine): Output list is empty.")
            }
            println("--- FE DEBUG (Combine): End ---")

            finalFilteredList
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

//    init {
//        loadEvents()
//    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: String) {
        val currentStatuses = _selectedStatuses.value.toMutableSet()
        if (status in currentStatuses) {
            currentStatuses.remove(status) // If it's already selected, deselect it.
        } else {
            currentStatuses.add(status) // If it's not selected, select it.
        }
        _selectedStatuses.value = currentStatuses
    }

    // --- 1. ADD THIS NEW FUNCTION ---
    /**
     * Explicitly clears any active status filter, resetting the view to "All".
     */
    fun clearStatusFilter() {
        _selectedStatuses.value = emptySet()
    }

    fun loadEvents(user: User?) { // <-- CHANGE 1: The parameter is now the full User object
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // --- CHANGE 2: THE DECISION LOGIC IS NOW MORE DETAILED ---
                val response = when {
                    // Case 1: Guest user (user is null).
                    user == null -> {
                        RetrofitInstance.api.getPublicEvents()
                    }
                    // Case 2: Management user. They also see the full public list.
                    user.role.equals("management", ignoreCase = true) -> {
                        RetrofitInstance.api.getAllEvents()
                    }
                    // Case 3: A regular, logged-in user.
                    else -> {
                        RetrofitInstance.api.getRecommendedEvents(user.userId)
                    }
                }

                if (response.isSuccessful && response.body() != null) {
                    val eventsFromServer = response.body()!!

                    // --- LOG 1: WHAT DID THE SERVER SEND? ---
                    println("FE DEBUG (loadEvents): API call successful. Received ${eventsFromServer.size} events.")
                    if (eventsFromServer.isNotEmpty()) {
                        println("FE DEBUG (loadEvents): First event from server is: '${eventsFromServer[0].eventName}'")
                    }

                    _uiState.update {
                        it.copy(isLoading = false, events = eventsFromServer)
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load events."
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred.") }
            }
        }
    }
}