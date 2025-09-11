package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
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
 *   <li>Datos personales básicos (nombre, apellido, email)</li>
 *   <li>Información de contacto (dirección, teléfono)</li>
 *   <li>Metadatos del sistema (rol, estado, fechas)</li>
 *   <li>Información de verificación y auditoría</li>
 * </ul>
 *
 * <p><strong>Información excluida por seguridad:</strong></p>
 * <ul>
 *   <li>Contraseña cifrada</li>
 *   <li>Tokens de sesión o verificación</li>
 *   <li>Información sensible de autenticación</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Respuestas de perfil de usuario</li>
 *   <li>Listados de usuarios en administración</li>
 *   <li>Información de cuidadores y clientes</li>
 *   <li>APIs de búsqueda de usuarios</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see User
 */
@Data
@Builder
public class UserResponse {

    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre del usuario.
     */
    private String firstName;

    /**
     * Apellido del usuario.
     */
    private String lastName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Funciona como identificador único para autenticación.</p>
     */
    private String email;

    /**
     * Dirección física del usuario.
     */
    private String address;

    /**
     * Número de teléfono del usuario.
     */
    private String phoneNumber;

    /**
     * Rol del usuario en el sistema.
     *
     * <p>Define los permisos y funcionalidades disponibles.</p>
     */

    private Role role;

    /**
     * Estado de activación de la cuenta.
     *
     * <p>Los usuarios inactivos no pueden acceder al sistema.</p>
     */
    private boolean isActive;

    /**
     * Fecha y hora de verificación del email.
     *
     * <p>Null si el email no ha sido verificado.</p>
     */
    private LocalDateTime emailVerifiedAt;

    /**
     * Fecha y hora del último login exitoso.
     */
    private LocalDateTime lastLoginAt;

    /**
     * Fecha y hora de creación del usuario.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     */
    private LocalDateTime updatedAt;

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Método de fábrica estático para convertir una entidad User a UserResponse DTO.
     *
     * <p>Este método centraliza la lógica de mapeo y mantiene el código limpio.
     * Maneja de forma segura los casos donde algunos campos pueden ser null.</p>
     *
     * @param user la entidad User a convertir
     * @return nueva instancia de UserResponse con datos poblados
     * @throws IllegalArgumentException si user es null
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
     * Retorna el nombre completo del usuario.
     *
     * @return nombre y apellido concatenados
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
     * @return true si el email está verificado
     */
    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    /**
     * Verifica si el usuario es un cuidador.
     *
     * @return true si el rol es SITTER
     */
    public boolean isSitter() {
        return role == Role.SITTER;
    }

    /**
     * Verifica si el usuario es un cliente.
     *
     * @return true si el rol es CLIENT
     */
    public boolean isClient() {
        return role == Role.CLIENT;
    }

    /**
     * Verifica si el usuario es administrador.
     *
     * @return true si el rol es ADMIN
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Proporciona una etiqueta de rol localizada para la UI.
     *
     * @return string representativo del rol para mostrar al usuario
     */
    public String getRoleLabel() {
        if (role == null) return "Sin Rol";

        switch (role) {
            case CLIENT: return "Cliente";
            case SITTER: return "Cuidador";
            case ADMIN: return "Administrador";
            default: return role.toString();
        }
    }

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