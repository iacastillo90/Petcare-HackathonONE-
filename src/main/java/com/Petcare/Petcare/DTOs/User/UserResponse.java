package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO para representar información completa de usuario en respuestas de API.
 *
 * <p>Este DTO expone todos los datos relevantes de un usuario sin información
 * sensible como contraseñas. Está optimizado para respuestas de API donde
 * se requiere información completa del perfil de usuario.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see User
 */
@Schema(description = "DTO con la información completa y pública de un usuario, excluyendo datos sensibles.")
public record UserResponse(
        @Schema(description = "Identificador único del usuario.", example = "1")
        Long id,

        @Schema(description = "Nombre de pila del usuario.", example = "John")
        String firstName,

        @Schema(description = "Apellido del usuario.", example = "Doe")
        String lastName,

        @Schema(description = "Dirección de correo electrónico del usuario, usada como identificador único para login.", example = "john.doe@example.com")
        String email,

        @Schema(description = "Dirección física del usuario.", example = "123 Main St, Springfield")
        String address,

        @Schema(description = "Número de teléfono de contacto del usuario.", example = "555-123-4567")
        String phoneNumber,

        @Schema(description = "Rol del usuario en el sistema, que define sus permisos.", example = "CLIENT")
        Role role,

        @Schema(description = "Indica si la cuenta del usuario está activa y puede iniciar sesión.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora en que el email fue verificado. Es nulo si aún no ha sido verificado.", example = "2025-02-10T15:30:00Z")
        LocalDateTime emailVerifiedAt,

        @Schema(description = "Fecha y hora del último inicio de sesión exitoso.", example = "2025-09-13T18:30:00Z")
        LocalDateTime lastLoginAt,

        @Schema(description = "Fecha y hora en que la cuenta del usuario fue creada.", example = "2025-02-10T15:25:00Z")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización del perfil del usuario.", example = "2025-09-13T18:30:00Z")
        LocalDateTime updatedAt
) {

    /**
     * Convierte una entidad del modelo {@link User} a un DTO {@link UserResponse}.
     *
     * <p>Este método de fábrica estático es la forma recomendada para crear instancias de {@code UserResponse}.
     * Se encarga de mapear de forma segura los campos de la entidad de persistencia a este DTO,
     * asegurando que no se exponga información sensible como la contraseña.</p>
     *
     * @param user la entidad User a convertir. No puede ser nula.
     * @return nueva instancia de UserResponse con datos poblados.
     * @throws IllegalArgumentException si el {@code user} proporcionado es nulo.
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("La entidad User no puede ser null");
        }
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAddress(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive(),
                user.getEmailVerifiedAt(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Retorna el nombre completo del usuario concatenando el nombre y el apellido.
     *
     * @return una cadena con el nombre completo, con espacios extra eliminados.
     */
    public String fullName() {
        if (lastName == null || lastName.trim().isEmpty()) {
            return firstName != null ? firstName : "";
        }
        return (firstName + " " + lastName).trim();
    }

    /**
     * Verifica si el email del usuario ha sido verificado.
     *
     * @return {@code true} si el email está verificado (timestamp no es nulo).
     */
    public boolean emailVerified() {
        return emailVerifiedAt != null;
    }

    /**
     * Verifica si el usuario tiene el rol de cuidador.
     *
     * @return {@code true} si el rol es SITTER.
     */
    public boolean isSitter() {
        return role == Role.SITTER;
    }

    /**
     * Verifica si el usuario tiene el rol de cliente.
     *
     * @return {@code true} si el rol es CLIENT.
     */
    public boolean isClient() {
        return role == Role.CLIENT;
    }

    /**
     * Verifica si el usuario tiene el rol de administrador.
     *
     * @return {@code true} si el rol es ADMIN.
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Proporciona una etiqueta de rol localizada y legible para la interfaz de usuario.
     *
     * @return una cadena representando el rol para mostrar al usuario (ej. "Cliente", "Cuidador").
     */
    public String roleLabel() {
        if (role == null) return "Sin Rol";
        return switch (role) {
            case CLIENT -> "Cliente";
            case SITTER -> "Cuidador";
            case ADMIN -> "Administrador";
        };
    }
}
