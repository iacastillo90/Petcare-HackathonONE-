package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import com.Petcare.Petcare.DTOs.User.DashboardStatsDTO;
import com.Petcare.Petcare.DTOs.User.MainDashboardDTO;
import com.Petcare.Petcare.DTOs.User.UserProfileDTO;
import com.Petcare.Petcare.DTOs.User.UserResponse;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.AccountService;
import com.Petcare.Petcare.Services.SitterService;
import com.Petcare.Petcare.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final SitterService sitterService;

    private final AccountRepository accountRepository;

    @GetMapping("/main")
    public ResponseEntity<MainDashboardDTO> getMainDashboardData(Authentication authentication) throws AccountNotFoundException {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);

        Long userId = user.getId();
        MainDashboardDTO dashboardData = userService.getMainDashboardData(userId);
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);

        Long userId = user.getId();

        DashboardStatsDTO stats = userService.getDashboardStatsForUser(userId);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();

        UserResponse user = userService.getUserByEmail(userEmail);


        Optional<Account> account = accountRepository.findByOwnerUserId(user.getId());

        // 2. Mapea la entidad User al UserProfileDTO
        UserProfileDTO userProfileDTO = new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(), // Convierte el Enum a String
                String.format("%c%c", user.getFirstName().charAt(0), user.getLastName().charAt(0)).toUpperCase(),
                account.get().getId()
        );

        // 3. Devuelve el DTO en la respuesta
        return ResponseEntity.ok(userProfileDTO);
    }

    /**
     * Endpoint para buscar y listar perfiles de cuidadores disponibles.
     * Requiere que el solicitante esté autenticado.
     *
     * @param authentication El principal del usuario autenticado, inyectado por Spring Security.
     * @param city Filtro opcional para buscar cuidadores en una ciudad específica.
     * @return Una lista de resúmenes de perfiles de cuidadores.
     */
    @GetMapping("/sitter-profiles")
    public ResponseEntity<List<SitterProfileSummary>> findAvailableSitters(
            Authentication authentication,
            @RequestParam(required = false) String city) {

        String email = authentication.getName();
        Optional<User> user = userRepository.findByEmail(email);


        List<SitterProfileSummary> sitters = sitterService.findSitters(city);
        return ResponseEntity.ok(sitters);
    }
}