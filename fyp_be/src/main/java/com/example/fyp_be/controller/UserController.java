package com.example.fyp_be.controller;

import com.example.fyp_be.model.LoginUserRequest;
import com.example.fyp_be.model.RegisterUserRequest;
import com.example.fyp_be.model.UpdateUserRequest;
import com.example.fyp_be.model.User;
import com.example.fyp_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public String getMessage() {
        return "Welcome";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest registerRequest) {
        try {
            Integer newUser = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserRequest loginRequest) {
        Optional<User> optionalUser = userService.loginUser(loginRequest);
        if (optionalUser.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");

            // Put the actual User object in the map
            response.put("user", optionalUser.get());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid identifier, password, or role"));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest updateUserRequest) {
        Optional<User> optionalUpdatedUser = userService.updateUser(id, updateUserRequest);
        if (optionalUpdatedUser.isPresent()) {
            User updatedUser = optionalUpdatedUser.get();
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found with ID: " + id));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
    }
}