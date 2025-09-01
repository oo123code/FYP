package com.example.fyp_be.service;

import com.example.fyp_be.model.LoginUserRequest;
import com.example.fyp_be.model.RegisterUserRequest;
import com.example.fyp_be.model.UpdateUserRequest;
import com.example.fyp_be.model.User;
import com.example.fyp_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Register User
    public Integer registerUser(RegisterUserRequest registerRequest) {

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Check for existing email
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Mapping
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());

        //need to decide on whether need to encrypt password later

        User savedUser = userRepository.save(user);
        System.out.println("Saved user ID: " + savedUser.getUserId());

        return savedUser.getUserId();
    }


    public Optional<User> loginUser(LoginUserRequest loginRequest) {
        String identifier = loginRequest.getIdentifier();
        String rawPassword = loginRequest.getPassword(); // The plain-text password from the FE
        String requestRole = loginRequest.getRole();   // The role the user selected on the FE

        // 1. Find the user by username or email (your logic is good here)
        Optional<User> optionalUser = userRepository.findByUsername(identifier);
        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByEmail(identifier);
        }

        // 2. If no user is found, return an empty Optional immediately.
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();
//        String actualHashedPassword = user.getPassword(); // The hashed password from the DB
        String actualRole = user.getRole();             // The actual role from the DB

        // 3. Perform the checks
        // FIX: Use the password encoder for a secure comparison.
            boolean passwordsMatch = user.getPassword().equalsIgnoreCase(rawPassword);

        // FIX: Compare the REQUESTED role with the ACTUAL role from the database.
        boolean rolesMatch = actualRole != null && actualRole.equalsIgnoreCase(requestRole);

        // 4. If both checks pass, return the User object. Otherwise, return empty.
        if (passwordsMatch && rolesMatch) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    public Optional<User> getUserProfile() {
        // In a real app, you'd get the user ID from the security context.
        // For now, we hardcode it to a test user ID, e.g., 1.
        Integer loggedInUserId = 1;
        return userRepository.findById(loggedInUserId);
    }

    public Optional<User> updateUser(Integer id, UpdateUserRequest updateRequest) {

        return userRepository.findById(id).map(user -> {
            if (updateRequest.getUsername()!=null) user.setUsername(updateRequest.getUsername());
            if (updateRequest.getEmail()!=null) user.setEmail(updateRequest.getEmail());
            if (updateRequest.getPassword()!=null) user.setPassword(updateRequest.getPassword());
            return userRepository.save(user);
        });
    }

    public boolean deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
