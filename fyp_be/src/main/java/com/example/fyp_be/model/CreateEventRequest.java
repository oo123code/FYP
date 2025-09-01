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

public class CreateEventRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventId;
    private String eventName;
    private String eventDescription;

    private String eventType; // Should I use Enum instead?
    private int eventMaxPax;
    private int eventAttendees = 0;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventStartTime;
    private LocalTime eventEndTime;

    private String eventLocation;
    private Integer createdBy; //I can use user type right? Since I can use userRepository.findById()
//    private LocalDateTime createdAt;

}