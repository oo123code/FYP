package com.example.fyp_be.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter

public enum EventStatus {
    UPCOMING("U"),
    CANCELLED("C"),
    ONGOING("O"),
    ENDED("E");

    private final String code;

    EventStatus(String code) {
        this.code = code;
    }
    @JsonValue  // Added this annotation to use the code for serialization
    public String getCodeForSerialization() {
        return this.name();
    }

    @JsonCreator
    public static EventStatus fromCode(String code) {
        for (EventStatus status : EventStatus.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
