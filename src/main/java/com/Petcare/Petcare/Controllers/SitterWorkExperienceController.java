package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;
import com.Petcare.Petcare.Services.SitterWorkExperienceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sitter-work-experience")
public class SitterWorkExperienceController {

    private final SitterWorkExperienceService workExperienceService;

    @Autowired
    public SitterWorkExperienceController(SitterWorkExperienceService workExperienceService) {
        this.workExperienceService = workExperienceService;
    }

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterWorkExperienceResponseDTO> createWorkExperience(@RequestBody SitterWorkExperienceRequestDTO requestDTO) {
        SitterWorkExperienceResponseDTO response = workExperienceService.createWorkExperience(requestDTO);
        return ResponseEntity.ok(response);
    }

    // ================= READ =================
    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<List<SitterWorkExperienceSummaryDTO>> getWorkExperiencesBySitter(@PathVariable Integer sitterId) {
        List<SitterWorkExperienceSummaryDTO> experiences = workExperienceService.getWorkExperiencesBySitterId(sitterId);
        return ResponseEntity.ok(experiences);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SitterWorkExperienceResponseDTO> getWorkExperienceById(@PathVariable Long id) {
        SitterWorkExperienceResponseDTO response = workExperienceService.getWorkExperienceById(id);
        return ResponseEntity.ok(response);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<SitterWorkExperienceResponseDTO> updateWorkExperience(@PathVariable Long id,
                                                                                @RequestBody SitterWorkExperienceRequestDTO requestDTO) {
        SitterWorkExperienceResponseDTO updated = workExperienceService.updateWorkExperience(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkExperience(@PathVariable Long id) {
        workExperienceService.deleteWorkExperience(id);
        return ResponseEntity.noContent().build();
    }

    // ================= CREATE ASYNC =================
    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<SitterWorkExperienceResponseDTO>> createWorkExperienceAsync(
            @RequestBody SitterWorkExperienceRequestDTO requestDTO) {
        return workExperienceService.createWorkExperienceAsync(requestDTO)
                .thenApply(ResponseEntity::ok);
    }
}
