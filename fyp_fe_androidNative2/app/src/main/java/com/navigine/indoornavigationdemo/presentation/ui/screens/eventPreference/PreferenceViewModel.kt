package com.navigine.indoornavigationdemo.presentation.ui.screens.eventPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.indoornavigationdemo.App
import com.navigine.indoornavigationdemo.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreferencesUiState(
    val isLoading: Boolean = false,
    // This will hold the preferences currently selected by the user in the UI.
    val selectedPreferences: Set<String> = emptySet(),
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val successMessage: String? = null
)

class PreferencesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState = _uiState.asStateFlow()

    // This is the master list of all possible categories.
    // In a more advanced app, this could be fetched from the server.
    val allCategories = listOf("Technology", "Business", "Art", "Music", "Healthcare", "Sports")

    /**
     * Fetches the user's currently saved preferences from the backend.
     */
    fun loadUserPreferences(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // NOTE: You will need to add a `getUserPreferences` function to your ApiService interface.
                val response = RetrofitInstance.api.getUserPreferences(userId)
                if (response.isSuccessful && response.body() != null) {
                    // We receive a List<String> and convert it to a Set for easier state management.
                    _uiState.update { it.copy(isLoading = false, selectedPreferences = response.body()!!.toSet()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load preferences.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Called from the UI when a user checks or unchecks a box.
     */
    fun onPreferenceToggle(category: String, isSelected: Boolean) {
        val currentPrefs = _uiState.value.selectedPreferences.toMutableSet()
        if (isSelected) {
            currentPrefs.add(category)
        } else {
            currentPrefs.remove(category)
        }
        _uiState.update { it.copy(selectedPreferences = currentPrefs) }
    }

    /**
     * Saves the user's currently selected preferences to the backend.
     */
    fun saveUserPreferences(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }
            try {
                // We convert our Set back to a List to send as the JSON body.
                val preferencesToSave = _uiState.value.selectedPreferences.toList()
                // NOTE: You will need to add an `updateUserPreferences` function to your ApiService interface.
                val response = RetrofitInstance.api.updateUserPreferences(userId, preferencesToSave)
                if (response.isSuccessful) {
                    // 2. Set the success message from the response body
                    val message = response.body()?.get("message") ?: "Preferences saved successfully!"

                    // --- THIS IS THE NEW LINE ---
                    // The preferences were saved, so we set the flag to true.
                    App.preferenceManager.setHasCompletedOnboarding(true)

                    _uiState.update { it.copy(isLoading = false, saveSuccess = true, successMessage = message) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to save preferences.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}