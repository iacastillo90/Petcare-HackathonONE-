package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;

/**
 * Async service wrapper for SitterWorkExperience operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SitterWorkExperienceAsyncService {

    private final SitterWorkExperienceService sitterWorkExperienceService;

    /**
     * Get work experiences by sitter profile ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<SitterWorkExperienceSummaryDTO>> getWorkExperiencesBySitterProfileIdAsync(Long sitterProfileId) {
        log.debug("Executing getWorkExperiencesBySitterProfileIdAsync({}) in background", sitterProfileId);
        try {
            List<SitterWorkExperienceSummaryDTO> experiences = sitterWorkExperienceService.getWorkExperiencesBySitterProfileId(sitterProfileId);
            return CompletableFuture.completedFuture(experiences);
        } catch (Exception e) {
            log.error("Error in getWorkExperiencesBySitterProfileIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get work experience by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<SitterWorkExperienceResponseDTO> getWorkExperienceByIdAsync(Long id) {
        log.debug("Executing getWorkExperienceByIdAsync({}) in background", id);
        try {
            SitterWorkExperienceResponseDTO experience = sitterWorkExperienceService.getWorkExperienceById(id);
            return CompletableFuture.completedFuture(experience);
        } catch (Exception e) {
            log.error("Error in getWorkExperienceByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
