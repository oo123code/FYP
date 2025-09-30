package com.example.fyp_be.service;// file: service/EventFeedbackService.java

import com.example.fyp_be.model.Event;
import com.example.fyp_be.model.EventFeedback;
import com.example.fyp_be.model.EventFeedbackRequest;
import com.example.fyp_be.model.User;
import com.example.fyp_be.repository.EventFeedbackRepository;
import com.example.fyp_be.repository.EventRegistrationRepository;
import com.example.fyp_be.repository.EventRepository;
import com.example.fyp_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EventFeedbackService {

    @Autowired
    private EventFeedbackRepository feedbackRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    /**
     * Creates and saves a new feedback entry for an event.
     *
     * @param eventId The ID of the event being reviewed.
     * @param request The DTO containing the user's ID, rating, and comment.
     * @return The newly saved EventFeedback object.
     * @throws IllegalStateException if feedback is not allowed (event not ended, duplicate submission).
     * @throws IllegalArgumentException if the event or user is not found.
     */
    public EventFeedback submitFeedback(Integer eventId, EventFeedbackRequest request) {
        // 1. Fetch the core entities.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        // --- THIS IS THE NEW VALIDATION LOGIC ---
        // 1. Check if the user was ever registered for this event.
        boolean wasRegistered = registrationRepository.existsByUser_UserIdAndEvent_EventId(request.getUserId(), eventId);
        if (!wasRegistered) {
            // Throw an exception if they were not registered.
            throw new IllegalStateException("You can only leave feedback for events you were registered for.");
        }
        // --- END OF NEW LOGIC ---

        // 2. Perform Business Logic Validation.
        if (!"ENDED".equalsIgnoreCase(String.valueOf(event.getEventStatus()))) {
            throw new IllegalStateException("Feedback can only be submitted for events that have ended.");
        }

        if (feedbackRepository.existsByUser_UserIdAndEvent_EventId(request.getUserId(), eventId)) {
            throw new IllegalStateException("You have already submitted feedback for this event.");
        }

        // 3. Create and save the new feedback entity.
        EventFeedback newFeedback = new EventFeedback();
        newFeedback.setEvent(event);
        newFeedback.setUser(user);
        newFeedback.setRating(request.getRating());
        newFeedback.setComment(request.getComment());

        return feedbackRepository.save(newFeedback);
    }

    public List<EventFeedback> getFeedbackForEvent(Integer eventId) {
        // This method simply calls the new repository function and returns the result.
        // In more complex applications, you could add more logic here, but for now,
        // a direct pass-through is perfect.
        return feedbackRepository.findByEvent_EventId(eventId);
    }
}