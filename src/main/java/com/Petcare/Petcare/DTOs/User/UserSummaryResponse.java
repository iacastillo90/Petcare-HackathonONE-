package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO para representar una vista resumida y optimizada de un usuario.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes para identificación
 * y toma de decisiones rápidas.</p>
 *
 * <p><strong>Información Incluida:</strong></p>
 * <ul>
 * <li>Identificador único del usuario.</li>
 * <li>Nombre completo formateado para presentación.</li>
 * <li>Email para identificación rápida.</li>
 * <li>Rol y estado para filtrado y control de acceso.</li>
 * <li>Fecha de creación para ordenamiento temporal.</li>
 * </ul>
 *
 * <p><strong>Casos de Uso Principales:</strong></p>
 * <ul>
 * <li>Listados paginados de usuarios en paneles de administración.</li>
 * <li>Respuestas de API para operaciones de búsqueda y autocompletado.</li>
 * <li>Selección de cuidadores o clientes en menús desplegables.</li>
 * <li>Vistas móviles o componentes de UI con espacio limitado.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see User
 * @see UserResponse
 */
@Schema(description = "DTO con información resumida de un usuario, optimizado para listados y vistas generales.")
public class UserSummaryResponse {

    /**
     * Identificador único del usuario.
     */
    @Schema(description = "Identificador único del usuario.", example = "1")
    private Long id;

    /**
     * Nombre completo del usuario formateado para presentación.
     *
     * <p>Combinación de firstName y lastName optimizada para mostrar
     * directamente en interfaces de usuario sin procesamiento adicional.</p>
     */
    @Schema(description = "Nombre completo del usuario, formateado para visualización.", example = "John Doe")
    private String fullName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Proporciona identificación única y contacto directo.
     * Esencial para selección de usuarios en operaciones administrativas.</p>
     */
    @Schema(description = "Dirección de correo electrónico del usuario, usada como identificador principal.", example = "john.doe@example.com")
    private String email;

    /**
     * Rol del usuario en el sistema.
     *
     * <p>Permite filtrado rápido por tipo de usuario y determina
     * acciones disponibles sin consultas adicionales.</p>
     */
    @Schema(description = "Rol del usuario en el sistema, que define sus permisos base.", example = "CLIENT")
    private Role role;

    /**
     * Estado de activación de la cuenta.
     *
     * <p>Campo crítico para identificar usuarios que pueden operar
     * en el sistema versus cuentas deshabilitadas.</p>
     */
    @Schema(description = "Indica si la cuenta del usuario está activa y puede operar en el sistema.", example = "true")
    private boolean isActive;

    /**
     * Estado de verificación del email.
     *
     * <p>Indicador rápido de confiabilidad del usuario sin necesidad
     * de verificar el timestamp completo.</p>
     */
    @Schema(description = "Indica si el correo electrónico del usuario ha sido verificado.", example = "true")
    private boolean emailVerified;

    /**
     * Fecha y hora de creación del usuario.
     *
     * <p>Útil para ordenamiento temporal y análisis de crecimiento
     * de la base de usuarios.</p>
     */
    @Schema(description = "Fecha y hora de creación de la cuenta del usuario.", example = "2025-02-10T15:25:00Z")
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización como Jackson.
     */
    public UserSummaryResponse() {
    }

    /**
     * Constructor completo para crear una instancia con todos los campos.
     * Útil para pruebas y construcción manual de objetos.
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
     * <p>
     * Este método de fábrica centraliza la lógica de mapeo, asegurando consistencia
     * y extrayendo solo la información esencial para esta vista resumida.
     * </p>
     * <ul>
     * <li>Combina `firstName` y `lastName` en `fullName`.</li>
     * <li>Convierte `emailVerifiedAt` (timestamp) a un booleano (`emailVerified`).</li>
     * <li>Preserva campos críticos para filtrado y visualización.</li>
     * </ul>
     *
     * @param user la entidad User a convertir. No puede ser nula.
     * @return una nueva instancia de UserSummaryResponse.
     * @throws IllegalArgumentException si el objeto user es nulo.
     */
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("La entidad User no puede ser null para la conversión a DTO.");
        }

        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(), // Usamos el método de la entidad para consistencia
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el ID del usuario.
     * @return el identificador único.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID del usuario.
     * @param id el nuevo identificador.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre completo del usuario.
     * @return el nombre completo.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Establece el nombre completo del usuario.
     * @param fullName el nuevo nombre completo.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Obtiene el email del usuario.
     * @return la dirección de correo electrónico.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el email del usuario.
     * @param email la nueva dirección de correo electrónico.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene el rol del usuario.
     * @return el rol.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario.
     * @param role el nuevo rol.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Verifica si la cuenta está activa.
     * @return {@code true} si la cuenta está activa.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Establece el estado de activación de la cuenta.
     * @param active el nuevo estado.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Verifica si el email ha sido verificado.
     * @return {@code true} si el email ha sido verificado.
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Establece el estado de verificación del email.
     * @param emailVerified el nuevo estado de verificación.
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Obtiene la fecha de creación de la cuenta.
     * @return el timestamp de creación.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece la fecha de creación de la cuenta.
     * @param createdAt el nuevo timestamp de creación.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Genera una representación de cadena del objeto para logging y debugging.
     * @return una cadena representando los campos clave del objeto.
     */
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