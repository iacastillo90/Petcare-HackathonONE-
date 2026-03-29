package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.User.UserResponse;
import com.Petcare.Petcare.DTOs.User.UserStatsResponse;
import com.Petcare.Petcare.DTOs.User.UserSummaryResponse;
import com.Petcare.Petcare.Models.User.Role;

/**
 * Async service wrapper for User operations.
 * 
 * This class provides asynchronous versions of user-related operations
 * that can be executed in the background to improve response times.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Service
public class UserAsyncService {

    private static final Logger log = LoggerFactory.getLogger(UserAsyncService.class);

    private final UserService userService;

    public UserAsyncService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users asynchronously.
     * Use this for admin dashboards to avoid blocking the main thread.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<UserResponse>> getAllUsersAsync() {
        log.debug("Executing getAllUsersAsync in background");
        try {
            List<UserResponse> users = userService.getAllUsers();
            return CompletableFuture.completedFuture(users);
        } catch (Exception e) {
            log.error("Error in getAllUsersAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get user by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<UserResponse> getUserByIdAsync(Long id) {
        log.debug("Executing getUserByIdAsync({}) in background", id);
        try {
            UserResponse user = userService.getUserById(id);
            return CompletableFuture.completedFuture(user);
        } catch (Exception e) {
            log.error("Error in getUserByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get users by role asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<UserSummaryResponse>> getUsersByRoleAsync(String role) {
        log.debug("Executing getUsersByRoleAsync({}) in background", role);
        try {
            Role userRole = Role.valueOf(role);
            List<UserSummaryResponse> users = userService.getUsersByRole(userRole);
            return CompletableFuture.completedFuture(users);
        } catch (Exception e) {
            log.error("Error in getUsersByRoleAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get user stats asynchronously.
     * Useful for dashboard statistics.
     */
    @Async("taskExecutor")
    public CompletableFuture<UserStatsResponse> getUserStatsAsync() {
        log.debug("Executing getUserStatsAsync in background");
        try {
            UserStatsResponse stats = userService.getUserStats();
            return CompletableFuture.completedFuture(stats);
        } catch (Exception e) {
            log.error("Error in getUserStatsAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
