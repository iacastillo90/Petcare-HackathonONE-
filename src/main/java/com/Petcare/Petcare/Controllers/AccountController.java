package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de cuentas.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Crear una nueva cuenta.
     *
     * @param request datos de la cuenta a crear
     * @param ownerUserId usuario propietario de la cuenta
     * @return la cuenta creada
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody CreateAccountRequest request,
            @RequestParam Long ownerUserId // Pasamos el owner por query param
    ) {
        // Aquí idealmente traeríamos el User desde la base de datos usando un service o repo

        User ownerUser = new User();
        ownerUser.setId(ownerUserId);

        AccountResponse response = accountService.createAccount(request, ownerUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener cuenta por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todas las cuentas.
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
}
