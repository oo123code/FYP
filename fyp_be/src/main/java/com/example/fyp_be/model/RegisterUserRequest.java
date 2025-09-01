package com.example.fyp_be.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class RegisterUserRequest {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String role;
}
