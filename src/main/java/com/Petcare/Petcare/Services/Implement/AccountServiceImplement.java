package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Models.Account.AccountUser;
import com.Petcare.Petcare.Models.User.PermissionLevel;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountUserRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImplement implements AccountService {

    private final AccountUserRepository accountUserRepository;
    private final UserRepository userRepository; // Puede que lo necesites

    @Transactional
    public void updateUserPermissions(User currentUser, PermissionLevel level) {

        Long currentUserId = currentUser.getId();

        // 1. Buscar la relación AccountUser del usuario actual.
        AccountUser accountUser = accountUserRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("No se encontró la membresía de la cuenta para el usuario."));

        // 2. Aplicar la lógica de negocio basada en el nivel seleccionado.
        boolean canManage = false;
        if (level == PermissionLevel.ADVANCED || level == PermissionLevel.PREMIUM) {
            canManage = true;
        }

        // 3. Actualizar los permisos en la entidad AccountUser y User.
        accountUser.setCanManagePets(canManage);
        accountUser.setCanManagePayments(canManage);
        currentUser.setPermissionLevel(level);

        // 4. Guardar los cambios.
        accountUserRepository.save(accountUser);
        userRepository.save(currentUser);

        log.info("Permisos actualizados para el usuario {} al nivel {}", currentUser.getEmail(), level);
    }
}
