package com.example.fyp_be.model;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    private String eventStatus;
    @Min(value = 1, message = "Maximum capacity must be at least 1.")
    private Integer eventMaxPax;
    private Integer eventAttendees;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventEndDate;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime eventStartTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime eventEndTime;

    private Integer editedByUserId;
    private String eventLocation;
    private String eventCategory;
}
