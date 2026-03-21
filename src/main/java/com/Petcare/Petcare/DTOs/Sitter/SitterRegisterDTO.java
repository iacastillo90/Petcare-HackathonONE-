package com.Petcare.Petcare.DTOs.Sitter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para encapsular los datos de registro de un nuevo usuario Cuidador (Sitter).
 *
 * <p>Esta clase se utiliza como cuerpo de la solicitud (Request Body) en el endpoint de
 * registro de cuidadores. Contiene toda la información básica necesaria para crear una
 * nueva entidad {@link com.Petcare.Petcare.Models.User.User} con el rol de 'SITTER'.</p>
 *
 * @author Equipo Petcare
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "DTO para encapsular los datos de registro de un nuevo usuario Cuidador (Sitter).")
public record SitterRegisterDTO(
        @Schema(description = "El nombre de pila del cuidador.", example = "Juan")
        String firstName,

        @Schema(description = "El apellido del cuidador.", example = "Pérez")
        String lastName,

        @Schema(description = "La dirección de correo electrónico del cuidador.", example = "juan.perez@example.com")
        String email,

        @Schema(description = "La contraseña para la nueva cuenta.", example = "securePassword123")
        String password,

        @Schema(description = "La dirección física del cuidador.", example = "Calle Principal 123, Ciudad")
        String address,

        @Schema(description = "El número de teléfono de contacto del cuidador.", example = "+57-300-123-4567")
        String phoneNumber
) {
}
