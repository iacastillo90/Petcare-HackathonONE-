package com.Petcare.Petcare.Controllers;


import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import jakarta.validation.Valid; // Para activar las validaciones del DTO
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sitter-profiles")
@RequiredArgsConstructor
public class SitterProfileController {

    private final UserRepository userRepository;
    private final SitterProfileRepository sitterProfileRepository;

    // Prueba


    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT')")
    @Transactional
    public ResponseEntity<?> createSitterProfile(
            @Valid @RequestBody SitterProfile request,
            @AuthenticationPrincipal User currentUser
    ) {
        if (sitterProfileRepository.findByUserId(currentUser.getId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User already has a sitter profile.");
        }

        SitterProfile profile = SitterProfile.builder()
                .user(currentUser)
                .bio(request.getBio())
                .hourlyRate(request.getHourlyRate())
                .isVerified(false)
                .isAvailableForBookings(true)
                .build();

        currentUser.setRole(Role.SITTER);

        userRepository.save(currentUser);
        SitterProfile savedProfile = sitterProfileRepository.save(profile);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
    }
}