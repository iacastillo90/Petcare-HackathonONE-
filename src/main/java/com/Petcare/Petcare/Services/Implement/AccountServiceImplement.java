package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AccountServiceImplement implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse createAccount(CreateAccountRequest request, User ownerUser) {
        // Crear la cuenta usando el usuario propietario recibido
        Account account = new Account(ownerUser, request.getAccountName(), request.getAccountNumber());
        accountRepository.save(account);

        return AccountResponse.fromEntity(account);
    }

    @Override
    @Cacheable(value = "accounts", key = "#id")
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return AccountResponse.fromEntity(account);
    }

    @Override
    @Cacheable(value = "accounts", key = "'all'")
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS ASYNC ==========

    @Async("taskExecutor")
    public CompletableFuture<List<AccountResponse>> getAllAccountsAsync() {
        log.debug("Executing getAllAccountsAsync in background thread");
        List<AccountResponse> accounts = getAllAccounts();
        return CompletableFuture.completedFuture(accounts);
    }

    @Async("taskExecutor")
    public CompletableFuture<AccountResponse> getAccountByIdAsync(Long id) {
        log.debug("Executing getAccountByIdAsync({}) in background thread", id);
        AccountResponse account = getAccountById(id);
        return CompletableFuture.completedFuture(account);
    }
}
