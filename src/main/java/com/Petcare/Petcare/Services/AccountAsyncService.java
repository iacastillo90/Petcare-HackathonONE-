package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;

/**
 * Async service wrapper for Account operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountAsyncService {

    private final AccountService accountService;

    /**
     * Get all accounts asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<AccountResponse>> getAllAccountsAsync() {
        log.debug("Executing getAllAccountsAsync in background");
        try {
            List<AccountResponse> accounts = accountService.getAllAccounts();
            return CompletableFuture.completedFuture(accounts);
        } catch (Exception e) {
            log.error("Error in getAllAccountsAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get account by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<AccountResponse> getAccountByIdAsync(Long id) {
        log.debug("Executing getAccountByIdAsync({}) in background", id);
        try {
            AccountResponse account = accountService.getAccountById(id);
            return CompletableFuture.completedFuture(account);
        } catch (Exception e) {
            log.error("Error in getAccountByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
