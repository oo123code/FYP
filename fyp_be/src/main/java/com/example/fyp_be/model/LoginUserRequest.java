package com.example.fyp_be.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class LoginUserRequest {
    private String identifier;
    private String password;
    private String role;
}
