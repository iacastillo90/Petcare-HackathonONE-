package com.Petcare.Petcare.Models.Auth.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    String firstname;
    @NotBlank(message = "Last name is required")
    String lastname;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password;
    @NotBlank(message = "Address is required")
    String address;
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}