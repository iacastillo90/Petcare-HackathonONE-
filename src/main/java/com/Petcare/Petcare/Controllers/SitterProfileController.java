package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.Services.SitterService;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sitter-profiles")
public class SitterProfileController {

    private final SitterService sitterService;
    private final UserRepository userRepository;

    @Autowired
    public SitterProfileController(SitterService sitterService,
                                   UserRepository userRepository) {
        this.sitterService = sitterService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createSitterProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody SitterProfileDTO sitterProfileDTO
    ) {
        log.info("Creando perfil de Sitter para userId={}", currentUser.getId());
        SitterProfileDTO created = sitterService.createSitterProfile(currentUser.getId(), sitterProfileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    public ResponseEntity<?> getSitterProfile(@PathVariable Long userId) {
        log.info("Obteniendo perfil de Sitter para userId={}", userId);
        SitterProfileDTO profileDTO = sitterService.getSitterProfile(userId);
        return ResponseEntity.ok(profileDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SitterProfileDTO>> getAllSitterProfiles() {
        log.info("Obteniendo todos los perfiles de Sitter");
        List<SitterProfileDTO> profiles = sitterService.getAllSitterProfiles();
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/{userId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    public ResponseEntity<?> updateSitterProfile(@PathVariable Long userId,
                                                 @RequestBody SitterProfileDTO sitterProfileDTO) {
        log.info("Actualizando perfil de Sitter para userId={}", userId);
        SitterProfileDTO updated = sitterService.updateSitterProfile(userId, sitterProfileDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    public ResponseEntity<?> deleteSitterProfile(@PathVariable Long userId) {
        log.info("Eliminando perfil de Sitter para userId={}", userId);
        sitterService.deleteSitterProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
