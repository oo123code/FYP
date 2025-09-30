package com.example.fyp_be.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "event_registrations")
@IdClass(EventRegistrationId.class)

public class EventRegistration {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "registration_timestamp", nullable = false)
    private LocalDateTime registrationTimestamp;

    @PrePersist
    protected void onCreate() {
        registrationTimestamp = LocalDateTime.now();
    }
}