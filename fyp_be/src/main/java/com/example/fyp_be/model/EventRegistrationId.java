package com.example.fyp_be.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class EventRegistrationId implements Serializable {

    private Integer user; // Corresponds to the 'user' field in the entity
    private Integer event; // Corresponds to the 'event' field in the entity

    // --- Must have a no-arg constructor, getters, setters ---

    // --- Must override equals() and hashCode() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventRegistrationId that = (EventRegistrationId) o;
        return Objects.equals(user, that.user) && Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, event);
    }
}