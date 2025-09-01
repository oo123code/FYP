package com.example.fyp_be.controller;

import com.example.fyp_be.model.CreateEventRequest;
import com.example.fyp_be.model.Event;
import com.example.fyp_be.model.UpdateEventRequest;
import com.example.fyp_be.model.User;
import com.example.fyp_be.repository.UserRepository;
import com.example.fyp_be.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<?> createEvent(@RequestBody CreateEventRequest createEventRequest) {
        Integer userId = createEventRequest.getCreatedBy();
        Event createdEvent = eventService.createEvent(createEventRequest, userId);
        String responseMessage = "Event created with ID: " + createdEvent.getEventId();
        return ResponseEntity.ok(responseMessage);
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
}
