package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de usuarios en operaciones generales del sistema.
 *
 * <p>Este DTO proporciona un balance entre la información completa de {@link UserResponse}
 * y la información mínima requerida para operaciones internas. Incluye validaciones
 * para casos donde se use en actualizaciones.</p>
 *
 * <p><strong>Diferencias con UserResponse:</strong></p>
 * <ul>
 *   <li>Incluye validaciones para operaciones de escritura</li>
 *   <li>Optimizado para transferencia interna entre capas</li>
 *   <li>Estructura más flexible para diferentes contextos</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Transferencia entre service layer y controller</li>
 *   <li>Operaciones internas que requieren validación</li>
 *   <li>Mapeos de datos para procesos de negocio</li>
 *   <li>DTOs base para otros DTOs especializados</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see User
 * @see UserResponse
 */
public class UserDTO {

    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre del usuario.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre no puede exceder 250 caracteres")
    private String firstName;

    /**
     * Apellido del usuario.
     */
    @Size(max = 250, message = "El apellido no puede exceder 250 caracteres")
    private String lastName;

    /**
     * Dirección de correo electrónico del usuario.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    @Size(max = 250, message = "El email no puede exceder 250 caracteres")
    private String email;

    /**
     * Dirección física del usuario.
     */
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

    /**
     * Número de teléfono del usuario.
     */
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    private String phoneNumber;

    /**
     * Rol del usuario en el sistema.
     */
    @NotNull(message = "El rol del usuario es obligatorio")
    private Role role;

    /**
     * Estado de activación de la cuenta.
     */
    private boolean isActive;

    /**
     * Fecha y hora de verificación del email.
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

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public UserDTO() {
    }

    /**
     * Constructor desde entidad User.
     *
     * @param user la entidad User a convertir
     */
    public UserDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            this.address = user.getAddress();
            this.phoneNumber = user.getPhoneNumber();
            this.role = user.getRole();
            this.isActive = user.isActive();
            this.emailVerifiedAt = user.getEmailVerifiedAt();
            this.lastLoginAt = user.getLastLoginAt();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }
    }

    /**
     * Constructor completo para testing y casos específicos.
     */
    public UserDTO(Long id, String firstName, String lastName, String email,
                   String address, String phoneNumber, Role role, boolean isActive,
                   LocalDateTime emailVerifiedAt, LocalDateTime lastLoginAt,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isActive = isActive;
        this.emailVerifiedAt = emailVerifiedAt;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== MÉTODOS DE FÁBRICA ==========

    /**
     * Crea una instancia de UserDTO desde una entidad User.
     *
     * @param user la entidad User a convertir
     * @return nueva instancia de UserDTO
     * @throws IllegalArgumentException si user es null
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("La entidad User no puede ser null");
        }
        return new UserDTO(user);
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
     * Verifica si el usuario puede realizar operaciones de cliente.
     *
     * @return true si el rol permite operaciones de cliente
     */
    public boolean canActAsClient() {
        return role == Role.CLIENT || role == Role.ADMIN;
    }

    /**
     * Verifica si el usuario puede realizar operaciones de cuidador.
     *
     * @return true si el rol permite operaciones de cuidador
     */
    public boolean canActAsSitter() {
        return role == Role.SITTER || role == Role.ADMIN;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}