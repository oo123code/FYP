package com.example.fyp_be.model;

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
    private String eventDescription;

    private String eventType;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;
    private LocalDateTime createdAt;

    private LocalDateTime editedAt;
    private Integer eventMaxPax;
    private Integer eventAttendees;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventStartTime;

    private LocalTime eventEndTime;
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "edited_by")
    private User editedBy;

    private String eventLocation;
}