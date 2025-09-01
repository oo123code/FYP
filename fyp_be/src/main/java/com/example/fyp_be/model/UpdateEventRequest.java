package com.example.fyp_be.model;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class UpdateEventRequest {
    private String eventName;
    private String eventDescription;
    private String eventType;

    @Enumerated(EnumType.STRING)
    private String eventStatus;
    private Integer eventMaxPax;
    private Integer attendees;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventStartTime;

    private LocalTime eventEndTime;
    private Integer editedByUserId; //I can use user type right? Since I can use userRepository.findById()
//    private LocalDateTime editedAt;
    private String eventLocation;
}
