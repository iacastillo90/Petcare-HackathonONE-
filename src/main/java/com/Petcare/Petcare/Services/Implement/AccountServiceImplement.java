package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
}
