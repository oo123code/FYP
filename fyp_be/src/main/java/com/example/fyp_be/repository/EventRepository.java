package com.example.fyp_be.repository;

import com.example.fyp_be.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    Optional<Event> findById(Integer eventId);
//    Optional<Event> findByEmail(String email);
}
