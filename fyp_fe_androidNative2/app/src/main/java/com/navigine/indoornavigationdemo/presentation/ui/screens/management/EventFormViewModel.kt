package com.navigine.indoornavigationdemo.presentation.ui.screens.management

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.data.CreateEventRequest // Ensure these import paths are correct for your project
import com.navigine.indoornavigationdemo.data.UpdateEventRequest // Ensure these import paths are correct
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Represents the state of the form fields themselves.
 * This is a top-level data class.
 */
data class EventFormState(
    val eventName: String = "",
    val eventDescription: String = "",
    val eventType: String = "",
    val eventLocation: String = "",
    val eventMaxPax: String = "",
    val eventAttendees: String = "",
    val eventStatus: String = "UPCOMING",
    val eventStartDate: String = "",
    val eventStartTime: String = "",
    val eventEndDate: String = "",
    val eventEndTime: String = "",
    val eventCategory: String = ""
)

/**
 * Represents the overall state of the screen, including loading and results.
 * This is also a top-level data class.
 */
data class EventFormUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val successMessage: String? = null
)

/**
 * This is the main ViewModel for the screen, now at the top level.
 */
class EventFormViewModel : ViewModel() {

    // --- STATE MANAGEMENT ---
    var formState by mutableStateOf(EventFormState())
        private set

    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState = _uiState.asStateFlow()

    private var currentEventId: Int? = null

    // 1. The full list of options for the dropdown.
    val categories = listOf("Technology", "Business", "Art", "Music", "Healthcare", "Sports", "Others")

    // 2. State to hold what is currently selected in the dropdown.
    var selectedDropdownCategory by mutableStateOf(categories[0]) // Default to the first item

    // 3. State to hold the text for the custom category input field.
    var customCategoryText by mutableStateOf("")

    fun onCategoryDropdownSelect(selection: String) {
        selectedDropdownCategory = selection
        // If the user selects anything other than "Others", we should clear
        // any old text from the custom input field to avoid confusion.
        if (selection != "Others") {
            customCategoryText = ""
        }
    }

    /**
     * A helper function to get the FINAL category value to be saved.
     * This is what you'll use when you build your Create/Update request object.
     */
    fun getFinalEventCategory(): String {
        return if (selectedDropdownCategory == "Others") {
            customCategoryText
        } else {
            selectedDropdownCategory
        }
    }

    // --- PUBLIC FUNCTIONS FOR THE UI ---

    fun loadEvent(eventId: Int?) {
        currentEventId = eventId
        if (eventId == null) {
            _uiState.update { it.copy(isEditMode = false) }
            formState = EventFormState()
        } else {
            _uiState.update { it.copy(isEditMode = true) }
            fetchEventDetails(eventId)
        }
    }

    fun onFormStateChange(newState: EventFormState) {
        formState = newState
    }

    fun saveEvent(currentUserId: Int) {
        // --- CHECKPOINT 1: Are we entering this function? ---
        println("CP 1: SAVE EVENT: Button clicked. Starting save process.")
        if (!validateInputs()) {
            // If validation fails, the function will stop here.
            println("CP 1.1: SAVE EVENT: Validation failed. Aborting.")
            return
        }

        // --- CHECKPOINT 2: Did validation pass? ---
        println("CP 2: SAVE EVENT: Validation successful. Proceeding to save.")

        if (_uiState.value.isEditMode) {
            updateEvent(currentUserId)
        } else {
            createEvent(currentUserId)
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveSuccess = false, error = null, successMessage = null) }
    }

    // --- PRIVATE LOGIC ---
    private fun fetchEventDetails(eventId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.api.getAllEvents()
                if (response.isSuccessful) {
                    val event = response.body()?.find { it.eventId == eventId }
                    if (event != null) {
                        formState = EventFormState(
                            eventName = event.eventName ?: "",
                            eventDescription = event.eventDescription ?: "",
                            eventType = event.eventType ?: "",
                            eventLocation = event.eventLocation ?: "",
                            eventMaxPax = event.eventMaxPax?.toString() ?: "",
                            eventAttendees = event.eventAttendees?.toString() ?: "",
                            eventStatus = event.eventStatus ?: "UPCOMING",
                            eventStartDate = parseDateTime(event.eventStartDateTime, "date"),
                            eventStartTime = parseDateTime(event.eventStartDateTime, "time"),
                            eventEndDate = parseDateTime(event.eventEndDateTime, "date"),
                            eventEndTime = parseDateTime(event.eventEndDateTime, "time"),
                            // --- ADD THIS ---
                            eventCategory = event.eventCategory ?: ""
                        )
                        // --- ADD THIS BLOCK TO SET THE DROPDOWN STATE ---
                        val category = event.eventCategory ?: ""
                        if (categories.contains(category)) {
                            selectedDropdownCategory = category
                            customCategoryText = ""
                        } else {
                            selectedDropdownCategory = "Others"
                            customCategoryText = category
                        }
                        // --- END OF NEW BLOCK ---

                    } else {
                        _uiState.update { it.copy(error = "Event not found.") }
                    }
                } else {
                    _uiState.update { it.copy(error = response.errorBody()?.string()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun createEvent(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = CreateEventRequest(
                createdByUserId = userId,
                eventName = formState.eventName,
                eventDescription = formState.eventDescription,
                eventType = formState.eventType,
                eventLocation = formState.eventLocation,
                eventMaxPax = formState.eventMaxPax.toInt(),
                eventStartDate = formState.eventStartDate,
                eventStartTime = formState.eventStartTime,
                eventEndDate = formState.eventEndDate,
                eventEndTime = formState.eventEndTime,
                eventCategory = getFinalEventCategory()
            )
            try {
                val response = RetrofitInstance.api.createEvent(request)
                if (response.isSuccessful) {
                    val newEvent = response.body()!!;
                    val successMsg = "Successfully created '${newEvent.eventName}' (ID: ${newEvent.eventId})."
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true, successMessage = successMsg) }
                } else {
                    val rawErrorBody = response.errorBody()?.string()
                    if (rawErrorBody != null) {
                        // This is a regular expression that looks for the pattern: "message":"<your message>"
                        // It then extracts only the part inside the quotes.
                        val messageRegex = """"message"\s*:\s*"([^"]*)"""".toRegex()
                        val matchResult = messageRegex.find(rawErrorBody)

                        // If we found a match, use our meaningful message.
                        // Otherwise, show a generic error.
                        val errorMessage = matchResult?.groups?.get(1)?.value ?: "An unknown server error occurred1."

                        _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "An unknown server error occurred2.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun updateEvent(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // --- CHECKPOINT 3: Are we trying to build the request object? ---
            println("CP 3: SAVE EVENT: In updateEvent. Building the request object...")
            val request = UpdateEventRequest(
                editedByUserId = userId,
                eventName = formState.eventName,
                eventDescription = formState.eventDescription,
                eventType = formState.eventType,
                eventLocation = formState.eventLocation,
                eventMaxPax = formState.eventMaxPax.toIntOrNull(),
                eventAttendees = formState.eventAttendees.toIntOrNull(),
                eventStatus = formState.eventStatus,
                eventStartDate = formState.eventStartDate,
                eventStartTime = formState.eventStartTime,
                eventEndDate = formState.eventEndDate,
                eventEndTime = formState.eventEndTime,
                eventCategory = getFinalEventCategory()
            )
            // --- CHECKPOINT 4: Was the request object built successfully? ---
            println("CP 4: SAVE EVENT: Request object built: $request")
            try {
                // --- CHECKPOINT 5: Are we about to make the API call? ---
                println("CP 5: SAVE EVENT: Calling Retrofit's updateEvent API function...")
                val response = RetrofitInstance.api.updateEvent(currentEventId!!, request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.errorBody()?.string()) }
                }
            } catch (e: Exception) {
                println("SAVE EVENT: CRASHED with exception: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (formState.eventName.isBlank() || formState.eventLocation.isBlank() ||
            formState.eventDescription.isBlank() || formState.eventType.isBlank() || formState.eventMaxPax.isBlank() ||
            formState.eventStartDate.isBlank() || formState.eventEndDate.isBlank() ||
            formState.eventStartTime.isBlank() || formState.eventEndTime.isBlank() ||
            selectedDropdownCategory == "Others" && customCategoryText.isBlank()) {
            _uiState.update { it.copy(error = "All fields are are mandatory.") }
            return false
        }

        return true
    }

    private fun parseDateTime(dateTimeString: String?, part: String): String {
        if (dateTimeString == null) return ""
        return try {
            val dateTime = LocalDateTime.parse(dateTimeString) //Call requires API level 26 (current min is 23):
            if (part == "date") {
                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE) // "yyyy-MM-dd"
            } else {
                dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME) // "HH:mm:ss"
            }
        } catch (e: Exception) {
            ""
        }
    }
}