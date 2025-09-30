package com.example.fyp_be.service;

import com.example.fyp_be.model.*;
import com.example.fyp_be.repository.EventRegistrationRepository;
import com.example.fyp_be.repository.EventRepository;
import com.example.fyp_be.repository.UserPreferenceRepository;
import com.example.fyp_be.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired // <-- ADD THIS NEW DEPENDENCY
    private EventRegistrationRepository registrationRepository;

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

        //Point 1: Role check
        if (!"management".equalsIgnoreCase(createdBy.getRole())) {
            throw new SecurityException("User does not have permission to create events.");
        }

        LocalDateTime newStartTime = LocalDateTime.of(createEventRequest.getEventStartDate(), createEventRequest.getEventStartTime());
        LocalDateTime newEndTime = LocalDateTime.of(createEventRequest.getEventEndDate(), createEventRequest.getEventEndTime());

        //Point 3: Data validation
        if (newEndTime.isBefore(newStartTime)) {
            throw new IllegalArgumentException("Validation failed: Event end time cannot be before the start time.");
        }

        List<Event> conflictingEvents = eventRepository.findConflictingEvents(
                createEventRequest.getEventLocation(),
                newStartTime,
                newEndTime
        );

        //Point 2:
        if (!conflictingEvents.isEmpty()) {
            // Get the first conflict to provide a helpful error message.
            Event existingEvent = conflictingEvents.get(0);

            String errorMessage = String.format(
                    "Scheduling conflict: The location '%s' is already booked by the event '%s' from %s to %s.", //show this line's message
                    createEventRequest.getEventLocation(),
                    existingEvent.getEventName(),
                    existingEvent.getEventStartDateTime().toLocalTime(),
                    existingEvent.getEventEndDateTime().toLocalTime()
            );

            throw new IllegalStateException(errorMessage);
        }

        //Point 4: Data persistence
        // Create a new Event entity
        Event event = new Event();

        // Set the fields from the CreateEventRequest to the Event entity
        event.setEventName(createEventRequest.getEventName());
        event.setEventDescription(createEventRequest.getEventDescription());
        event.setEventType(createEventRequest.getEventType());
        event.setEventMaxPax(createEventRequest.getEventMaxPax());
        event.setEventAttendees(0);
        // 3. Set the new combined fields on the entity before saving
        event.setEventStartDateTime(newStartTime);
        event.setEventEndDateTime(newEndTime);
        event.setEventLocation(createEventRequest.getEventLocation());
        event.setEventCategory(createEventRequest.getEventCategory());

        // Set the default status
        event.setEventStatus(EventStatus.UPCOMING); // Default status

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

            // Refactor: 1. Combine DTO fields into LocalDateTime objects
            LocalDateTime newStartTime = LocalDateTime.of(updatedEventRequest.getEventStartDate(), updatedEventRequest.getEventStartTime());
            LocalDateTime newEndTime = LocalDateTime.of(updatedEventRequest.getEventEndDate(), updatedEventRequest.getEventEndTime());

            if (newEndTime.isBefore(newStartTime)) {
                throw new IllegalArgumentException("Validation failed: Event end time cannot be before the start time.");
            }

            List<Event> conflictingEvents = eventRepository.findConflictingEventsExcludeId(
                    updatedEventRequest.getEventLocation(),
                    newStartTime,
                    newEndTime,
                    eventId
            );

            if (!conflictingEvents.isEmpty()) {
                String errorMessage = String.format(
                        "Scheduling conflict: The location '%s' is already booked by the event '%s' from %s to %s.",
                        updatedEventRequest.getEventLocation(),
                        existingEvent.getEventName(),
                        existingEvent.getEventStartDateTime().toLocalTime(), // Shows the time
                        existingEvent.getEventEndDateTime().toLocalTime()   // Shows the time
                );
                throw new IllegalStateException(errorMessage);
            }

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
            if (updatedEventRequest.getEventAttendees() != null) existingEvent.setEventAttendees(updatedEventRequest.getEventAttendees());

            existingEvent.setEventStartDateTime(newStartTime);
            existingEvent.setEventEndDateTime(newEndTime);

            if (updatedEventRequest.getEventLocation() != null) existingEvent.setEventLocation(updatedEventRequest.getEventLocation());
            if (updatedEventRequest.getEventCategory() != null) existingEvent.setEventCategory(updatedEventRequest.getEventCategory());

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

    public List<Event> getRecommendedEvents(Integer userId) {
        System.out.println("\nDEBUG: STARTING getRecommendedEvents for userId: " + userId + " ---");

        // Level 1: Check the Input Data
        List<String> rawUserPreferences = userPreferenceService.getUserPreferences(userId);
        System.out.println("Step 1.1: Raw preferences from DB: " + rawUserPreferences);

        List<String> userPreferences = rawUserPreferences.stream()
                .map(pref -> pref.trim().toLowerCase())
                .collect(Collectors.toList());
        System.out.println("Step 1.2: Normalized preferences (lowercase, trimmed): " + userPreferences);

        List<Event> allRelevantEvents = eventRepository.findAllUpcoming();
        System.out.println("Step 2: Found " + allRelevantEvents.size() + " upcoming/ongoing events.");

        if (userPreferences.isEmpty()) {
            System.out.println("Step 3: User has no preferences. Returning unsorted list.");
            System.out.println("DEBUG: END ---");
            return allRelevantEvents;
        }

        // Level 2: Log the state BEFORE sorting
        // Print the first event's name to see if the list order changes.
        if (!allRelevantEvents.isEmpty()) {
            System.out.println("Step 3: Before Sort, first event is: '" + allRelevantEvents.get(0).getEventName() + "'");
        }

        // Level 3: The Core Logic - Log INSIDE the Comparator
        System.out.println("\n--- Step 4: Starting Sort. Analyzing each event... ---");
        allRelevantEvents.sort(Comparator.comparing(event -> {
            String rawEventCategory = event.getEventCategory();
            String eventCategory = (rawEventCategory != null) ? rawEventCategory.trim().toLowerCase() : "";

            boolean isPreferred = userPreferences.contains(eventCategory);

            // This log is the most important. It shows the decision for every event.
            System.out.println(
                    "  - Event: '" + event.getEventName() + "'" +
                            " | Category: [\"" + eventCategory + "\"]" +
                            " | Is Preferred? " + isPreferred +
                            " | Assigning Score: " + (isPreferred ? 0 : 1)
            );

            return isPreferred ? 0 : 1;
        }));
        System.out.println("--- End of Sort Analysis ---\n");

        // Level 4: Log the state AFTER sorting
        if (!allRelevantEvents.isEmpty()) {
            System.out.println("Step 5: After Sort, first event is: '" + allRelevantEvents.get(0).getEventName() + "'");
        }

        System.out.println(" DEBUG: END");
        return allRelevantEvents;
    }

    public List<Event> getPublicEvents() {
        return eventRepository.findAllUpcoming();
    }

    public Event registerUserForEvent(Integer eventId, Integer userId) {
        // 1. Find the entities. Throws an exception if not found.
        System.out.println("--- SERVICE: Starting registration logic for eventId: " + eventId + " and userId: " + userId + " ---");
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if ("management".equalsIgnoreCase(user.getRole())) {
            throw new IllegalStateException("Management accounts cannot register for events.");
        }

        // 2. Perform Business Logic Validation.
        if (!"UPCOMING".equalsIgnoreCase(String.valueOf(event.getEventStatus())) && !"ONGOING".equalsIgnoreCase(String.valueOf(event.getEventStatus()))) {
            throw new IllegalStateException("This event is not active and cannot be registered for.");
        }

        if (registrationRepository.existsByUser_UserIdAndEvent_EventId(userId, eventId)) {
            throw new IllegalStateException("You are already registered for this event.");
        }

        if (event.getEventAttendees() >= event.getEventMaxPax()) {
            throw new IllegalStateException("Sorry, this event is already full.");
        }

        // 3. All checks passed. Perform the database updates.
        // Increment the attendee count on the event itself.
        event.setEventAttendees(event.getEventAttendees() + 1);
        Event updatedEvent = eventRepository.save(event);

        // Create a new record in the linking table.
        EventRegistration registration = new EventRegistration();
        registration.setUser(user);
        registration.setEvent(updatedEvent);
        registrationRepository.save(registration);

        // 4. Return the updated event.
        return updatedEvent;
    }

    public List<Event> getRegisteredEventsForUser(Integer userId) {
        // 1. Use the repository to find all registration records for this user.
        List<EventRegistration> registrations = registrationRepository.findByUser_UserId(userId);

        // 2. Transform the list of `EventRegistration` objects into a list of `Event` objects.
        // For each registration, we get the associated Event.
        return registrations.stream()
                .map(EventRegistration::getEvent)
                .collect(Collectors.toList());
    }

    public Event unregisterUserFromEvent(Integer eventId, Integer userId) {
        // 1. Find the event entity. This also validates that the event exists.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        // 2. Perform Business Logic Validation.
        // Check if a registration actually exists. If not, we can't unregister them.
        if (!registrationRepository.existsByUser_UserIdAndEvent_EventId(userId, eventId)) {
            throw new IllegalStateException("You are not registered for this event.");
        }

        // 3. All checks passed. Perform the database updates.
        // First, delete the record from the linking table.
        registrationRepository.deleteByUser_UserIdAndEvent_EventId(userId, eventId);

        // Then, decrement the attendee count on the event itself.
        // We add a check to prevent the count from going below zero, just in case.
        if (event.getEventAttendees() > 0) {
            event.setEventAttendees(event.getEventAttendees() - 1);
        }

        // Save the updated event and return it.
        return eventRepository.save(event);
    }
}