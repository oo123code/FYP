package com.example.fyp_be.service;

import com.example.fyp_be.model.User;
import com.example.fyp_be.model.UserPreference;
import com.example.fyp_be.repository.UserPreferenceRepository;
import com.example.fyp_be.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserPreferenceService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Fetches the list of preference strings (e.g., "Technology", "Art") for a given user.
     */
    public List<String> getUserPreferences(Integer userId) {
        List<UserPreference> preferences = userPreferenceRepository.findByUser_UserId(userId);
        // Convert the list of UserPreference objects into a simple list of category strings.
        return preferences.stream()
                .map(UserPreference::getEventCategory)
                .collect(Collectors.toList());
    }

    /**
     * Overwrites a user's existing preferences with a new list of categories.
     */
    @Transactional // This ensures that both delete and save operations are one atomic transaction.
    public void updateUserPreferences(Integer userId, List<String> newCategories) {
        // First, find the User entity. We can't save a preference without it.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 1. Delete all of the user's old preferences.
        userPreferenceRepository.deleteByUser_UserId(userId);

        // 2. Create and save the new UserPreference objects from the list of strings.
        List<UserPreference> newPreferences = newCategories.stream()
                .map(category -> {
                    UserPreference pref = new UserPreference();
                    pref.setUser(user);
                    pref.setEventCategory(category);
                    return pref;
                })
                .collect(Collectors.toList());

        userPreferenceRepository.saveAll(newPreferences);
    }
}