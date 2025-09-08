package com.Petcare.Petcare.DTOs.Auth.Respone;

import com.Petcare.Petcare.DTOs.User.UserProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    String token;
    private String role;
    private UserProfileDTO userProfile;
}
