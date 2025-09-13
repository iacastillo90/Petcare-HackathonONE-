package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO que representa un perfil de usuario simplificado y seguro para la interfaz de cliente.
 * <p>
 * Este objeto inmutable ({@code record}) está diseñado para transportar la información esencial
 * y no sensible de un usuario después de una autenticación exitosa. Es ideal para ser
 * utilizado por el frontend para mostrar datos en la cabecera, menús de perfil o
 * para identificar al usuario actual sin exponer detalles innecesarios.
 * </p>
 *
 * @param id          El identificador único del usuario.
 * @param firstName   El nombre de pila del usuario.
 * @param lastName    El apellido del usuario.
 * @param email       La dirección de correo electrónico del usuario.
 * @param role        El rol principal del usuario en el sistema (ej. "CLIENT", "SITTER").
 * @param initials    Las iniciales del usuario (ej. "JD" para John Doe), generadas para avatares de UI.
 * @param accountId   El identificador de la cuenta principal a la que pertenece el usuario.
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see AuthResponse
 */
@Schema(description = "Perfil de usuario simplificado con la información esencial para mostrar en la interfaz de cliente.")
public record UserProfileDTO (

        @Schema(description = "Identificador único del usuario.", example = "1")
        Long id,

        @Schema(description = "Nombre de pila del usuario.", example = "John")
        String firstName,

        @Schema(description = "Apellido del usuario.", example = "Doe")
        String lastName,

        @Schema(description = "Dirección de correo electrónico del usuario.", example = "john.doe@example.com")
        String email,

        @Schema(description = "Rol del usuario en el sistema.", example = "CLIENT")
        String role,

        @Schema(description = "Iniciales del usuario, generadas para avatares en la UI.", example = "JD")
        String initials,

        @Schema(description = "ID de la cuenta principal a la que pertenece el usuario.", example = "1")
        Long accountId

) { }