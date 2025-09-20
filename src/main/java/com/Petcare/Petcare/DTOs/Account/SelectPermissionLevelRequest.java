package com.Petcare.Petcare.DTOs.Account;

import com.Petcare.Petcare.Models.User.PermissionLevel;
import jakarta.validation.constraints.NotNull;

// Usamos un 'record' por ser conciso e inmutable, ideal para DTOs.
public record SelectPermissionLevelRequest(
        @NotNull(message = "El nivel de permiso es obligatorio.")
        PermissionLevel level
) {}