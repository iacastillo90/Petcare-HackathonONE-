package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.User.DashboardStatsDTO;
import com.Petcare.Petcare.DTOs.User.MainDashboardDTO;
import com.Petcare.Petcare.DTOs.User.UserProfileDTO;
import com.Petcare.Petcare.DTOs.User.UserResponse;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService; // O un DashboardService dedicado

/*    @GetMapping("/main")
    public ResponseEntity<MainDashboardDTO> getMainDashboardData(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        Long userId = user.getId();
        //MainDashboardDTO dashboardData = userService.getMainDashboardData(userId);
        return ResponseEntity.ok(dashboardData);
    }*/

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        Long userId = user.getId();

        DashboardStatsDTO stats = userService.getDashboardStatsForUser(userId);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();

        UserResponse user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + userEmail));

        // 2. Mapea la entidad User al UserProfileDTO
        UserProfileDTO userProfileDTO = new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(), // Convierte el Enum a String
                // Lógica para las iniciales
                String.format("%c%c", user.getFirstName().charAt(0), user.getLastName().charAt(0)).toUpperCase()
        );

        // 3. Devuelve el DTO en la respuesta
        return ResponseEntity.ok(userProfileDTO);
    }
}