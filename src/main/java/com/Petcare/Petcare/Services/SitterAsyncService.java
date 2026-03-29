package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;

/**
 * Async service wrapper for Sitter operations.
 * 
 * This class provides asynchronous versions of sitter profile operations
 * that can be executed in the background to improve response times.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SitterAsyncService {

    private final SitterService sitterService;

    /**
     * Get all sitter profiles asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<SitterProfileDTO>> getAllSitterProfilesAsync() {
        log.debug("Executing getAllSitterProfilesAsync in background");
        try {
            List<SitterProfileDTO> profiles = sitterService.getAllSitterProfiles();
            return CompletableFuture.completedFuture(profiles);
        } catch (Exception e) {
            log.error("Error in getAllSitterProfilesAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Find sitters by city asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<SitterProfileSummary>> findSittersAsync(String city) {
        log.debug("Executing findSittersAsync({}) in background", city);
        try {
            List<SitterProfileSummary> sitters = sitterService.findSitters(city);
            return CompletableFuture.completedFuture(sitters);
        } catch (Exception e) {
            log.error("Error in findSittersAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get sitter profile by user ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<SitterProfileDTO> getSitterProfileAsync(Long userId) {
        log.debug("Executing getSitterProfileAsync({}) in background", userId);
        try {
            SitterProfileDTO profile = sitterService.getSitterProfile(userId);
            return CompletableFuture.completedFuture(profile);
        } catch (Exception e) {
            log.error("Error in getSitterProfileAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
