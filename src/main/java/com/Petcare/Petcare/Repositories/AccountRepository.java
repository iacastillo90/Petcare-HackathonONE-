package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByOwnerUser(User authenticatedUser);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByOwnerUserId(Long userId);
}
