package com.example.fyp_be.repository;

import com.example.fyp_be.model.Event;
//import org.gradle.internal.time.ExponentialBackoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    Optional<Event> findById(Integer eventId);

    // This query is for CREATING. It checks against ALL events.
    @Query("SELECT e FROM Event e WHERE e.eventLocation = :location " +
            "AND e.eventEndDateTime > :newStartDateTime " +
            "AND e.eventStartDateTime < :newEndDateTime")
    List<Event> findConflictingEvents(
            @Param("location") String location,
            @Param("newStartDateTime") LocalDateTime newStartDateTime,
            @Param("newEndDateTime") LocalDateTime newEndDateTime
    );

    // This query is for UPDATING. It checks against all events EXCEPT for one.
    @Query("SELECT e FROM Event e WHERE e.eventLocation = :location " +
            "AND e.eventEndDateTime > :newStartDateTime " +
            "AND e.eventStartDateTime < :newEndDateTime " +
            "AND e.eventId != :excludeEventId")
    List<Event> findConflictingEventsExcludeId(
            @Param("location") String location,
            @Param("newStartDateTime") LocalDateTime newStartDateTime,
            @Param("newEndDateTime") LocalDateTime newEndDateTime,
            @Param("excludeEventId") Integer excludeEventId
    );

    @Query("SELECT e FROM Event e WHERE e.eventCategory IN :categories")
    List<Event> findRecommendedEvents(@Param("categories") List<String> categories);

    @Query("SELECT e FROM Event e WHERE e.eventStatus != 'ENDED'")
    List<Event> findAllUpcoming();
}