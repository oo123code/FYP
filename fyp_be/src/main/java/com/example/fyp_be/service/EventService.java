package com.example.fyp_be.service;

import com.example.fyp_be.model.*;
import com.example.fyp_be.repository.EventRepository;
import com.example.fyp_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // Get event by ID or all events if no ID is provided
    public List<Event> getEventById(Integer eventId) {
        if (eventId == null) {
            // Return all events if no eventId is provided
            return eventRepository.findAll();
        } else {
            // Return the specific event if eventId is provided
            return eventRepository.findById(eventId)
                    .map(List::of)  // Wrap the found event in a list
                    .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        }
    }

    // Create event
    public Event createEvent(CreateEventRequest createEventRequest, Integer userId) {
        // Fetch the user who is creating the event
        User createdBy = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        //Role check
        if (!"management".equalsIgnoreCase(createdBy.getRole())) {
            throw new SecurityException("User does not have permission to create events.");
        }

        // Create a new Event entity
        Event event = new Event();

        // Set the fields from the CreateEventRequest to the Event entity
        event.setEventName(createEventRequest.getEventName());
        event.setEventDescription(createEventRequest.getEventDescription());
        event.setEventType(createEventRequest.getEventType());
        event.setEventMaxPax(createEventRequest.getEventMaxPax());
        event.setEventAttendees(createEventRequest.getEventAttendees());
        event.setEventStartDate(createEventRequest.getEventStartDate());
        event.setEventStartTime(createEventRequest.getEventStartTime());
        event.setEventEndDate(createEventRequest.getEventEndDate());
        event.setEventEndTime(createEventRequest.getEventEndTime());
        event.setEventLocation(createEventRequest.getEventLocation());

        // Set the default status
        event.setEventStatus(EventStatus.valueOf("UPCOMING")); // Default status

        // Set the user who created the event
        event.setCreatedBy(createdBy);
        event.setCreatedAt(LocalDateTime.now());

        // Save and return the event
        return eventRepository.save(event);
    }

    public Optional<Event> updateEvent(Integer eventId, UpdateEventRequest updatedEventRequest) {
        Optional<Event> existingEventOpt = eventRepository.findById(eventId);

        if (existingEventOpt.isPresent()) {
            Event existingEvent = existingEventOpt.get();

            // --- THIS IS THE NEW CORE LOGIC FOR editedBy ---
            // 1. Get the user ID from the request DTO.
            Integer userId = updatedEventRequest.getEditedByUserId();
            if (userId == null) {
                // It's good practice to require this.
                throw new IllegalArgumentException("Editor user ID must be provided.");
            }

            // 2. Find the user in the database.
            User editor = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Editor user not found with ID: " + userId));

            // 3. (Optional but Recommended) Add your simple security check.
            if (!"management".equalsIgnoreCase(editor.getRole())) {
                throw new SecurityException("User does not have permission to edit events.");
            }
            // --- END OF NEW LOGIC ---

            // Update logic
            if (updatedEventRequest.getEventName() != null) existingEvent.setEventName(updatedEventRequest.getEventName());
            if (updatedEventRequest.getEventDescription() != null) existingEvent.setEventDescription(updatedEventRequest.getEventDescription());
            if (updatedEventRequest.getEventType() != null) existingEvent.setEventType(updatedEventRequest.getEventType());

            if (updatedEventRequest.getEventStatus() != null)
            {
                String statusCode = updatedEventRequest.getEventStatus();

                // Use your custom fromCode method to find the correct enum (e.g., EventStatus.CANCELLED)
                EventStatus status = EventStatus.fromCode(statusCode); //How to put equals ignore case here

                // Set the enum on the entity. The @Enumerated annotation will handle saving it to the DB correctly.
                existingEvent.setEventStatus(status);
            }
            if (updatedEventRequest.getEventMaxPax() != null) existingEvent.setEventMaxPax(updatedEventRequest.getEventMaxPax());
            if (updatedEventRequest.getAttendees() != null) existingEvent.setEventAttendees(updatedEventRequest.getAttendees());

            if (updatedEventRequest.getEventStartDate() != null) existingEvent.setEventStartDate(updatedEventRequest.getEventStartDate());
            if (updatedEventRequest.getEventStartTime() != null) existingEvent.setEventStartTime(updatedEventRequest.getEventStartTime());
            if (updatedEventRequest.getEventEndDate() != null) existingEvent.setEventEndDate(updatedEventRequest.getEventEndDate());

            if (updatedEventRequest.getEventEndTime() != null) existingEvent.setEventEndTime(updatedEventRequest.getEventEndTime());
            if (updatedEventRequest.getEventLocation() != null) existingEvent.setEventLocation(updatedEventRequest.getEventLocation());

            existingEvent.setEditedAt(LocalDateTime.now());
            existingEvent.setEditedBy(editor);

            return Optional.of(eventRepository.save(existingEvent));
        }
        return Optional.empty();  // Return empty if event is not found
    }

    // Delete event
    public void deleteEvent(Integer eventId) {

        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event not found");
        }
        eventRepository.deleteById(eventId);
    }

}
