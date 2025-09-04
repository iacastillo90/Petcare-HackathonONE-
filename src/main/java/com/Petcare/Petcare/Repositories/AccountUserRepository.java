package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Account.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {

    /**
     * Verifica si existe una relación entre una cuenta y un usuario.
     * @param accountId ID de la cuenta.
     * @param userId ID del usuario.
     * @return true si el usuario es miembro de la cuenta, false en caso contrario.
     */
    boolean existsByAccountIdAndUserId(Long accountId, Long userId);

    Optional<AccountUser> findByUserId(Long currentUserId);
}