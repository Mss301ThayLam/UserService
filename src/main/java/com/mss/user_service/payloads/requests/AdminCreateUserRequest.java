package com.mss.user_service.payloads.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminCreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phoneNumber;
    private String password;
    private String gender;
    private String dateOfBirth;
    private List<String> roles;
}
