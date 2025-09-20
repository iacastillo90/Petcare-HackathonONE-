package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO para representar información completa de usuario en respuestas de API.
 *
 * <p>Este DTO expone todos los datos relevantes de un usuario sin información
 * sensible como contraseñas. Está optimizado para respuestas de API donde
 * se requiere información completa del perfil de usuario.</p>
 *
 * <p><strong>Información incluida:</strong></p>
 * <ul>
 * <li>Datos personales básicos (nombre, apellido, email)</li>
 * <li>Información de contacto (dirección, teléfono)</li>
 * <li>Metadatos del sistema (rol, estado, fechas)</li>
 * <li>Información de verificación y auditoría</li>
 * </ul>
 *
 * <p><strong>Información excluida por seguridad:</strong></p>
 * <ul>
 * <li>Contraseña cifrada</li>
 * <li>Tokens de sesión o verificación</li>
 * <li>Información sensible de autenticación</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 * <li>Respuestas de perfil de usuario</li>
 * <li>Listados de usuarios en administración</li>
 * <li>Información de cuidadores y clientes</li>
 * <li>APIs de búsqueda de usuarios</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see User
 */
@Data
@Builder
@Schema(description = "DTO con la información completa y pública de un usuario, excluyendo datos sensibles.")
public class UserResponse {

    /**
     * Identificador único del usuario.
     */
    @Schema(description = "Identificador único del usuario.", example = "1")
    private Long id;

    /**
     * Nombre del usuario.
     */
    @Schema(description = "Nombre de pila del usuario.", example = "John")
    private String firstName;

    /**
     * Apellido del usuario.
     */
    @Schema(description = "Apellido del usuario.", example = "Doe")
    private String lastName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Funciona como identificador único para autenticación.</p>
     */
    @Schema(description = "Dirección de correo electrónico del usuario, usada como identificador único para login.", example = "john.doe@example.com")
    private String email;

    /**
     * Dirección física del usuario.
     */
    @Schema(description = "Dirección física del usuario.", example = "123 Main St, Springfield")
    private String address;

    /**
     * Número de teléfono del usuario.
     */
    @Schema(description = "Número de teléfono de contacto del usuario.", example = "555-123-4567")
    private String phoneNumber;

    /**
     * Rol del usuario en el sistema.
     *
     * <p>Define los permisos y funcionalidades disponibles.</p>
     */
    @Schema(description = "Rol del usuario en el sistema, que define sus permisos.", example = "CLIENT")
    private Role role;

    /**
     * Estado de activación de la cuenta.
     *
     * <p>Los usuarios inactivos no pueden acceder al sistema.</p>
     */
    @Schema(description = "Indica si la cuenta del usuario está activa y puede iniciar sesión.", example = "true")
    private boolean isActive;

    /**
     * Fecha y hora de verificación del email.
     *
     * <p>Null si el email no ha sido verificado.</p>
     */
    @Schema(description = "Fecha y hora en que el email fue verificado. Es nulo si aún no ha sido verificado.", example = "2025-02-10T15:30:00Z")
    private LocalDateTime emailVerifiedAt;

    /**
     * Fecha y hora del último login exitoso.
     */
    @Schema(description = "Fecha y hora del último inicio de sesión exitoso.", example = "2025-09-13T18:30:00Z")
    private LocalDateTime lastLoginAt;

    /**
     * Fecha y hora de creación del usuario.
     */
    @Schema(description = "Fecha y hora en que la cuenta del usuario fue creada.", example = "2025-02-10T15:25:00Z")
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     */
    @Schema(description = "Fecha y hora de la última actualización del perfil del usuario.", example = "2025-09-13T18:30:00Z")
    private LocalDateTime updatedAt;

    // ========== MÉTODO DE FÁBRICA ==========

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

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.isActive())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Retorna el nombre completo del usuario concatenando el nombre y el apellido.
     *
     * @return una cadena con el nombre completo, con espacios extra eliminados.
     */
    public String getFullName() {
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
    public boolean isEmailVerified() {
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
    public String getRoleLabel() {
        if (role == null) return "Sin Rol";

        return switch (role) {
            case CLIENT -> "Cliente";
            case SITTER -> "Cuidador";
            case ADMIN -> "Administrador";
        };
    }

    /**
     * Genera una representación de cadena del objeto para logging y debugging.
     * <p><strong>Nota de Seguridad:</strong> Se excluye información sensible. Esta representación
     * es segura para ser registrada en logs.</p>
     * @return una cadena representando los campos clave del objeto.
     */
    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", emailVerified=" + isEmailVerified() +
                ", createdAt=" + createdAt +
                '}';
    }
}