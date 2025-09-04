package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Account.SelectPermissionLevelRequest;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Operaciones relacionadas con la cuenta del usuario")
public class AccountController {

    private final AccountService accountService;

    private final UserRepository userRepository;

    @PostMapping("/my-account/permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> selectPermissionLevel(
            @Valid @RequestBody SelectPermissionLevelRequest request,
            Authentication authentication // <-- 2. Recibes el objeto Authentication
    ) {
        // 3. Obtén el email (username) del usuario autenticado
        String userEmail = authentication.getName();

        // 4. Busca la entidad User completa en la base de datos
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));

        // 5. Llama al servicio con la entidad User recuperada
        accountService.updateUserPermissions(currentUser, request.level());

        return ResponseEntity.ok().build();
    }
}