package com.example.fyp_be.repository;

import com.example.fyp_be.model.EventFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Integer> {

    /**
     * Checks if a feedback record already exists for a given user and event.
     * This is crucial for preventing duplicate submissions.
     * Spring Data JPA will build the query automatically from this method name.
     */
    boolean existsByUser_UserIdAndEvent_EventId(Integer userId, Integer eventId);

    List<EventFeedback> findByEvent_EventId(Integer eventId);
}