package com.example.fyp_be.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class UserPreferenceId implements Serializable {

    private Integer user; // Must match the field name in UserPreference entity
    private String eventCategory;

    // Default constructor, getters, setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreferenceId that = (UserPreferenceId) o;
        return Objects.equals(user, that.user) && Objects.equals(eventCategory, that.eventCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, eventCategory);
    }
}