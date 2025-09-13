package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * <li>Incluye validaciones para operaciones de escritura</li>
 * <li>Optimizado para transferencia interna entre capas</li>
 * <li>Estructura más flexible para diferentes contextos</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 * <li>Transferencia entre service layer y controller</li>
 * <li>Operaciones internas que requieren validación</li>
 * <li>Mapeos de datos para procesos de negocio</li>
 * <li>DTOs base para otros DTOs especializados</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see User
 * @see UserResponse
 */
@Schema(description = "DTO para transferir información de usuarios en operaciones generales del sistema.")
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
     * Constructor para crear una instancia del DTO a partir de una entidad {@link User}.
     *
     * @param user la entidad User a convertir.
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
     * Constructor completo para facilitar la creación de instancias en código, como en pruebas.
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
     * @param user la entidad User a convertir.
     * @return nueva instancia de UserDTO.
     * @throws IllegalArgumentException si user es null.
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("La entidad User no puede ser null");
        }
        return new UserDTO(user);
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
     * Verifica si el usuario puede realizar operaciones de cliente.
     *
     * @return {@code true} si el rol es CLIENT o ADMIN.
     */
    public boolean canActAsClient() {
        return role == Role.CLIENT || role == Role.ADMIN;
    }

    /**
     * Verifica si el usuario puede realizar operaciones de cuidador.
     *
     * @return {@code true} si el rol es SITTER o ADMIN.
     */
    public boolean canActAsSitter() {
        return role == Role.SITTER || role == Role.ADMIN;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el identificador único del usuario.
     * @return el ID del usuario.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el identificador único del usuario.
     * @param id el nuevo ID para el usuario.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del usuario.
     * @return el nombre del usuario.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Establece el nombre del usuario.
     * @param firstName el nuevo nombre del usuario.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Obtiene el apellido del usuario.
     * @return el apellido del usuario.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Establece el apellido del usuario.
     * @param lastName el nuevo apellido del usuario.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Obtiene la dirección de correo electrónico del usuario.
     * @return la dirección de email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece la dirección de correo electrónico del usuario.
     * @param email la nueva dirección de email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la dirección física del usuario.
     * @return la dirección física.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Establece la dirección física del usuario.
     * @param address la nueva dirección física.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Obtiene el número de teléfono del usuario.
     * @return el número de teléfono.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Establece el número de teléfono del usuario.
     * @param phoneNumber el nuevo número de teléfono.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Obtiene el rol del usuario.
     * @return el rol del usuario.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario.
     * @param role el nuevo rol del usuario.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Verifica si la cuenta del usuario está activa.
     * @return {@code true} si la cuenta está activa.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Establece el estado de activación de la cuenta.
     * @param active {@code true} para activar, {@code false} para desactivar.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Obtiene el timestamp de verificación de email.
     * @return la fecha y hora de verificación, o {@code null} si no está verificado.
     */
    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    /**
     * Establece el timestamp de verificación de email.
     * @param emailVerifiedAt la nueva fecha y hora de verificación.
     */
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    /**
     * Obtiene el timestamp del último inicio de sesión.
     * @return la fecha y hora del último login.
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Establece el timestamp del último inicio de sesión.
     * @param lastLoginAt la nueva fecha y hora del último login.
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * Obtiene el timestamp de creación del usuario.
     * @return la fecha y hora de creación.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece el timestamp de creación del usuario.
     * @param createdAt la nueva fecha y hora de creación.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtiene el timestamp de la última actualización.
     * @return la fecha y hora de la última actualización.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Establece el timestamp de la última actualización.
     * @param updatedAt la nueva fecha y hora de la última actualización.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Genera una representación de cadena del DTO para logging y debugging.
     * @return una cadena representando los campos principales del objeto.
     */
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