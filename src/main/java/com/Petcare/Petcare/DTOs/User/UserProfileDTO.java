package com.Petcare.Petcare.DTOs.User;

public record UserProfileDTO (
    Long id,
    String firstName,
    String lastName,
    String email,
    String role,
    String initials,
    Long accountId

) { }
