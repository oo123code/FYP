package com.example.fyp_be.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table (name = "events")

public class Event {

    //Add img src path later on when stable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventId;
    private String eventName;
    @Column(columnDefinition = "TEXT")
    private String eventDescription;

    private String eventType;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;
    private LocalDateTime createdAt;

    private LocalDateTime editedAt;
    private Integer eventMaxPax;
    private Integer eventAttendees;

    @Column(name = "event_start_datetime")
    private LocalDateTime eventStartDateTime;

    @Column(name = "event_end_datetime")
    private LocalDateTime eventEndDateTime;

    @ManyToOne
    @JoinColumn(name = "created_by")
//    @JsonBackReference("user-created-events")
    @JsonIgnore
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "edited_by")
//    @JsonBackReference("user-edited-events")
    @JsonIgnore
    private User editedBy;

    private String eventLocation;

    private String eventCategory;

    public Integer getCreatedByUserId() {
        if (createdBy != null) {
            return createdBy.getUserId(); // Assumes your User entity has a getUserId() method
        }
        return null;
    }

    // Jackson will see this and add an "editedByUserId" field to the JSON.
    public Integer getEditedByUserId() {
        if (editedBy != null) {
            return editedBy.getUserId();
        }
        return null;
    }

    public String getEventStatus() {
        // Rule 1: If an event was manually cancelled, it stays cancelled. This is a final state.
        if ("CANCELLED".equalsIgnoreCase(String.valueOf(this.eventStatus))) {
            return "CANCELLED";
        }

        // Rule 2: If an event was manually marked as ended, it stays ended. (Optional but good)
        if ("ENDED".equalsIgnoreCase(String.valueOf(this.eventStatus))) {
            return "ENDED";
        }

        // Rule 3: Calculate the status based on the current time.
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.eventStartDateTime)) {
            return "UPCOMING";
        } else if (now.isAfter(this.eventEndDateTime)) {
            return "ENDED";
        } else {
            return "ONGOING";
        }
    }
}