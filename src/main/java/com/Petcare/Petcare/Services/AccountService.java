package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.Models.User.User;
import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request, User ownerUser);

    AccountResponse getAccountById(Long id);

    List<AccountResponse> getAllAccounts();
}
