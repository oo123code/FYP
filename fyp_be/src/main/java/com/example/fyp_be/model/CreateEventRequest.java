package com.example.fyp_be.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class CreateEventRequest {
    @NotNull(message = "Creator user ID is mandatory.")
    private Integer createdByUserId;

    @NotBlank(message = "Event name is mandatory.")
    private String eventName;

    @NotBlank(message = "Event description is mandatory.")
    private String eventDescription;

    @NotBlank(message = "Event type is mandatory.")
    private String eventType;

    @NotBlank(message = "Event location is mandatory.")
    private String eventLocation;

    @NotNull(message = "Maximum capacity is mandatory.")
    @Min(value = 1, message = "Capacity must be at least 1.")
    private Integer eventMaxPax; // Use Integer to allow @NotNull

    @NotNull(message = "Event start date is mandatory.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventStartDate;

    @NotNull(message = "Event end date is mandatory.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventEndDate;

    @NotNull(message = "Event start time is mandatory.")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime eventStartTime;

    @NotNull(message = "Event end time is mandatory.")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime eventEndTime;

    @NotNull(message = "Maximum capacity is mandatory.")
    private Integer eventAttendees =0;

    @NotBlank(message = "Event category is mandatory.")
    private String eventCategory;
}