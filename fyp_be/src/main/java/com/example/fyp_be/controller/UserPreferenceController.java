package com.example.fyp_be.controller;

import com.example.fyp_be.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/userPreference") // A good base path for user-related actions
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    /**
     * API endpoint to get a user's preferences.
     * Example URL: GET /api/v1/users/1/preferences
     */
    @GetMapping("/{userId}/preferences")
    public ResponseEntity<List<String>> getUserPreferences(@PathVariable Integer userId) {
        List<String> preferences = userPreferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * API endpoint to update a user's preferences.
     * The new preferences are sent in the request body as a JSON array of strings.
     * Example URL: PUT /api/v1/users/1/preferences
     * Example Body: ["Technology", "Art", "Sports"]
     */
    @PutMapping("/{userId}/preferences")
    public ResponseEntity<?> updateUserPreferences(@PathVariable Integer userId, @RequestBody List<String> newPreferences
    ) {
        userPreferenceService.updateUserPreferences(userId, newPreferences);

        // --- CHANGE THESE LINES ---
        // Create a Map to hold the response.
        Map<String, String> response = new HashMap<>();
        response.put("message", "Preferences updated successfully.");

        // Spring will automatically serialize this Map to: {"message": "Preferences updated successfully."}
        return ResponseEntity.ok(response);
    // --- END OF CHANGES ---
    }
}