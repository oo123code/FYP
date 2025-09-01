package com.example.fyp_be.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // A simple GET endpoint that returns a dummy message
    @GetMapping("/test") // The URL path to access this endpoint
    public String getTestMessage() {
        return "Welcome";
    }
}