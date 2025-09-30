package com.example.fyp_be.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data

public class EventFeedbackRequest {
    @NotNull(message = "User ID is required.")
    private Integer userId;

    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be no more than 5.")
    private int rating;

    private String comment;

}
