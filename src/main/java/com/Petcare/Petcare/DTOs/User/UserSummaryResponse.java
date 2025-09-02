package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;

import java.time.LocalDateTime;

/**
 * DTO para representar una vista resumida y optimizada de un usuario.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes para identificación
 * y toma de decisiones rápidas.</p>
 *
 * <p><strong>Información incluida:</strong></p>
 * <ul>
 *   <li>Identificador único del usuario</li>
 *   <li>Nombre completo formateado para presentación</li>
 *   <li>Email para identificación rápida</li>
 *   <li>Rol y estado para filtrado y control de acceso</li>
 *   <li>Fecha de creación para ordenamiento temporal</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Listados paginados de usuarios</li>
 *   <li>Dashboards administrativos</li>
 *   <li>Respuestas de API para operaciones de búsqueda</li>
 *   <li>Selección de cuidadores o clientes</li>
 *   <li>Vistas móviles con espacio limitado</li>
 * </ul>
 *
 * <p><strong>Optimizaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Solo campos esenciales para reducir payload</li>
 *   <li>Nombre completo preformateado</li>
 *   <li>Sin información sensible o detallada</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see User
 * @see UserResponse
 */
public class UserSummaryResponse {

    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre completo del usuario formateado para presentación.
     *
     * <p>Combinación de firstName y lastName optimizada para mostrar
     * directamente en interfaces de usuario sin procesamiento adicional.</p>
     */
    private String fullName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Proporciona identificación única y contacto directo.
     * Esencial para selección de usuarios en operaciones administrativas.</p>
     */
    private String email;

    /**
     * Rol del usuario en el sistema.
     *
     * <p>Permite filtrado rápido por tipo de usuario y determina
     * acciones disponibles sin consultas adicionales.</p>
     */
    private Role role;

    /**
     * Estado de activación de la cuenta.
     *
     * <p>Campo crítico para identificar usuarios que pueden operar
     * en el sistema versus cuentas deshabilitadas.</p>
     */
    private boolean isActive;

    /**
     * Estado de verificación del email.
     *
     * <p>Indicador rápido de confiabilidad del usuario sin necesidad
     * de verificar el timestamp completo.</p>
     */
    private boolean emailVerified;

    /**
     * Fecha y hora de creación del usuario.
     *
     * <p>Útil para ordenamiento temporal y análisis de crecimiento
     * de la base de usuarios.</p>
     */
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public UserSummaryResponse() {
    }

    /**
     * Constructor completo para creación programática y testing.
     *
     * @param id identificador del usuario
     * @param fullName nombre completo formateado
     * @param email dirección de correo electrónico
     * @param role rol del usuario
     * @param isActive estado de activación
     * @param emailVerified estado de verificación de email
     * @param createdAt fecha de creación
     */
    public UserSummaryResponse(Long id, String fullName, String email, Role role,
                               boolean isActive, boolean emailVerified, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
    }

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Crea una instancia de UserSummaryResponse desde una entidad User.
     *
     * <p>Este método de fábrica extrae solo la información esencial y realiza
     * el formateo necesario para optimizar la presentación.</p>
     *
     * <p><strong>Procesamiento aplicado:</strong></p>
     * <ul>
     *   <li>Combina firstName y lastName en fullName</li>
     *   <li>Convierte emailVerifiedAt a boolean emailVerified</li>
     *   <li>Preserva campos críticos para filtrado</li>
     * </ul>
     *
     * @param user la entidad User a convertir
     * @return nueva instancia de UserSummaryResponse
     * @throws IllegalArgumentException si user es null
     */
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("La entidad User no puede ser null");
        }

        return new UserSummaryResponse(
                user.getId(),
                formatFullName(user.getFirstName(), user.getLastName()),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getEmailVerifiedAt() != null,
                user.getCreatedAt()
        );
    }

    /**
     * Formatea un nombre completo de manera segura.
     *
     * @param firstName nombre
     * @param lastName apellido
     * @return nombre completo formateado, o string vacío si ambos son null
     */
    private static String formatFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "";
        }

        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        return (first + " " + last).trim();
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si el usuario puede actuar como cuidador.
     *
     * @return true si el rol permite servicios de cuidado
     */
    public boolean canProvideCareServices() {
        return isActive && (role == Role.SITTER || role == Role.ADMIN);
    }

    /**
     * Verifica si el usuario puede solicitar servicios como cliente.
     *
     * @return true si el rol permite solicitar servicios
     */
    public boolean canRequestServices() {
        return isActive && (role == Role.CLIENT || role == Role.ADMIN);
    }

    /**
     * Verifica si el usuario requiere verificación de email.
     *
     * @return true si el email no está verificado y la cuenta está activa
     */
    public boolean requiresEmailVerification() {
        return isActive && !emailVerified;
    }

    /**
     * Proporciona una etiqueta de estado combinada para la UI.
     *
     * @return string representativo del estado general del usuario
     */
    public String getStatusLabel() {
        if (!isActive) {
            return "Inactivo";
        }
        if (!emailVerified) {
            return "Pendiente verificación";
        }
        return "Activo";
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

    /**
     * Genera iniciales del nombre completo para avatars.
     *
     * @return iniciales del usuario (máximo 2 caracteres)
     */
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "??";
        }

        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }

        return initials.length() > 0 ? initials.toString().toUpperCase() : "??";
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserSummaryResponse{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", emailVerified=" + emailVerified +
                ", createdAt=" + createdAt +
                '}';
    }
}