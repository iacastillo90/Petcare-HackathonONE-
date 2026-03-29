package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Pet.PetResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;

/**
 * Async service wrapper for Pet operations.
 * 
 * Provides asynchronous versions of pet-related operations
 * for better performance and non-blocking execution.
 */
@Service
public class PetAsyncService {

    private static final Logger log = LoggerFactory.getLogger(PetAsyncService.class);

    private final PetService petService;

    public PetAsyncService(PetService petService) {
        this.petService = petService;
    }

    /**
     * Get all pets asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<PetResponse>> getAllPetsAsync() {
        log.debug("Executing getAllPetsAsync in background");
        try {
            List<PetResponse> pets = petService.getAllPets();
            return CompletableFuture.completedFuture(pets);
        } catch (Exception e) {
            log.error("Error in getAllPetsAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get pet by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<PetResponse> getPetByIdAsync(Long id) {
        log.debug("Executing getPetByIdAsync({}) in background", id);
        try {
            PetResponse pet = petService.getPetById(id);
            return CompletableFuture.completedFuture(pet);
        } catch (Exception e) {
            log.error("Error in getPetByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get pets by account asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<PetResponse>> getPetsByAccountIdAsync(Long accountId) {
        log.debug("Executing getPetsByAccountIdAsync({}) in background", accountId);
        try {
            List<PetResponse> pets = petService.getPetsByAccountId(accountId);
            return CompletableFuture.completedFuture(pets);
        } catch (Exception e) {
            log.error("Error in getPetsByAccountIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
