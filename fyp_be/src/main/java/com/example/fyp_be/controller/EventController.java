package com.example.fyp_be.controller;

import com.example.fyp_be.model.*;
import com.example.fyp_be.repository.EventRegistrationRepository;
import com.example.fyp_be.repository.EventRepository;
import com.example.fyp_be.repository.UserRepository;
import com.example.fyp_be.service.EventFeedbackService;
import com.example.fyp_be.service.EventService;
import com.example.fyp_be.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventFeedbackService eventFeedbackService;

    @GetMapping("/test")
    public String getMessage() {
        return "Welcome";
    }

    // Get event by ID or all events
    @GetMapping("/getEvent")
    public ResponseEntity<List<Event>> getEvent(@RequestParam(required = false) Integer eventId) {
        List<Event> events = eventService.getEventById(eventId);
        return ResponseEntity.ok(events);
    }

    // Create event
    //Check if current user role is "Management", if yes only then proceed
    @PostMapping("/createEvent")
    public ResponseEntity<?> createEvent(@Validated @RequestBody CreateEventRequest createEventRequest) {
        try {
            Integer userId = createEventRequest.getCreatedByUserId();
            Event createdEvent = eventService.createEvent(createEventRequest, userId);
            return ResponseEntity.ok(createdEvent);

        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            String errorMessage = e.getMessage();

            Map<String, String> errorResponse = Map.of("message", errorMessage);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    // Update event
    @PutMapping("/updateEvent/{eventId}")
    public ResponseEntity<Event> updateEvent(@PathVariable Integer eventId, @RequestBody @Validated UpdateEventRequest updateEventRequest) {
        Optional<Event>  updatedEvent= eventService.updateEvent(eventId, updateEventRequest);
        return updatedEvent
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete event
    @DeleteMapping("/deleteEvent/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Integer eventId) {
        eventService.deleteEvent(eventId);
        String responseMessage = "Event deleted with ID: " + eventId;
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getRecommendedEvents/{userId}")
    public ResponseEntity<List<Event>> getRecommendedEvents(@PathVariable Integer userId) { // <-- FIXES ARE HERE
        // 1. This method's ONLY job is to call the service.
        List<Event> recommendedEvents = eventService.getRecommendedEvents(userId);

        // 2. It then wraps the result in a ResponseEntity and sends it back.
        return ResponseEntity.ok(recommendedEvents);
    }

    @GetMapping("/getPublicEvents")
    public ResponseEntity<List<Event>> getPublicEvents() {
        List<Event> publicEvents = eventService.getPublicEvents();
        return ResponseEntity.ok(publicEvents);
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<?> registerForEvent(@PathVariable Integer eventId, @RequestBody EventRegistrationRequest request) {
        try {
            Event updatedEvent = eventService.registerUserForEvent(eventId, request.getUserId());
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException e) {
            // This is for 404 Not Found
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            // --- CHANGE 2: THIS IS THE CRITICAL FIX ---
            // This is for 409 Conflict. We now create a custom response body.
            Map<String, String> errorResponse = new HashMap<>();
            // We take the specific message from the service exception and put it in our response.
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @GetMapping("/{userId}/registered-events")
    public ResponseEntity<List<Event>> getRegisteredEvents(@PathVariable Integer userId) {
        List<Event> registeredEvents = eventService.getRegisteredEventsForUser(userId);
        return ResponseEntity.ok(registeredEvents);
    }

    @DeleteMapping("/{eventId}/register") // Using the same path as POST, but with a different HTTP method
    public ResponseEntity<Event> unregisterFromEvent(@PathVariable Integer eventId, @RequestBody EventRegistrationRequest request // We can reuse the same request body DTO
    ) {
        try {
            Event updatedEvent = eventService.unregisterUserFromEvent(eventId, request.getUserId());
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            // 409 Conflict is also a good status for trying to unregister when you're not registered.
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{eventId}/is-registered")
    public ResponseEntity<Map<String, Boolean>> checkRegistrationStatus(@PathVariable Integer eventId, @RequestParam Integer userId) {
        boolean isRegistered = eventRegistrationRepository.existsByUser_UserIdAndEvent_EventId(userId, eventId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isRegistered", isRegistered);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Integer eventId, @RequestBody EventFeedbackRequest request) {
        try {
            // --- FIX 1: USE THE INJECTED SERVICE INSTANCE ---
            EventFeedback savedFeedback = eventFeedbackService.submitFeedback(eventId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());

            // --- FIX 2: RETURN THE MAP DIRECTLY, WITHOUT CASTING ---
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @GetMapping("/{eventId}/feedback")
    public ResponseEntity<List<EventFeedback>> getFeedbackForEvent(@PathVariable Integer eventId) {
        // 1. Call the service method to get the data.
        List<EventFeedback> feedbackList = eventFeedbackService.getFeedbackForEvent(eventId);

        // 2. Return the list with a 200 OK status.
        return ResponseEntity.ok(feedbackList);
    }
}