package com.example.fyp_be.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table (name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String firstName;
    private String lastName;

    @Column (unique = true)
    private String username;

    @Column (unique = true)
    private String email;

    private String password;
    private String role ;

    @OneToMany(mappedBy = "createdBy")
//    @JsonManagedReference("user-created-events") // <-- ADD THIS
    private List<Event> createdEvents;

    @OneToMany(mappedBy = "editedBy")
//    @JsonManagedReference("user-edited-events") // <-- ADD THIS
    private List<Event> editedEvents;
}