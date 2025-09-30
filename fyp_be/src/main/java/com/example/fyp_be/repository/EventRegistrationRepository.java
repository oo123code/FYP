package com.example.fyp_be.repository;

import com.example.fyp_be.model.EventRegistration;
import com.example.fyp_be.model.EventRegistrationId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, EventRegistrationId> {

    // quickly check if a row exists for a specific user and event combination.
    boolean existsByUser_UserIdAndEvent_EventId(Integer userId, Integer eventId);

    List<EventRegistration> findByUser_UserId(Integer userId);

    @Modifying
    @Transactional
    void deleteByUser_UserIdAndEvent_EventId(Integer userId, Integer eventId);
}