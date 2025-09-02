package com.Petcare.Petcare.Models.User;


import com.Petcare.Petcare.Models.Account.AccountUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un usuario del sistema Petcare.
 *
 * <p>Esta entidad implementa {@link UserDetails} de Spring Security para integración
 * con el sistema de autenticación y autorización. Maneja tanto usuarios clientes
 * como cuidadores de mascotas.</p>
 *
 * <p><strong>Roles principales:</strong></p>
 * <ul>
 *   <li>CLIENT: Usuario propietario de mascotas que solicita servicios</li>
 *   <li>SITTER: Usuario cuidador que ofrece servicios de cuidado</li>
 *   <li>ADMIN: Usuario administrador del sistema</li>
 * </ul>
 *
 * <p><strong>Características de seguridad:</strong></p>
 * <ul>
 *   <li>Integración completa con Spring Security</li>
 *   <li>Control de estado de cuenta (activo/inactivo)</li>
 *   <li>Verificación de email</li>
 *   <li>Seguimiento de último login</li>
 * </ul>
 *
 * <p><strong>Membresías multi-cuenta:</strong></p>
 * <p>Un usuario puede pertenecer a múltiples cuentas familiares a través
 * de la entidad intermedia {@link AccountUser}, permitiendo gestión
 * colaborativa de mascotas.</p>
 *
 * <p><strong>Nota:</strong> Las validaciones de negocio complejas y lógica
 * de autenticación se manejan en el service layer.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see UserDetails
 * @see Role
 * @see AccountUser
 */
@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_role", columnList = "role"),
                @Index(name = "idx_user_active", columnList = "isActive"),
                @Index(name = "idx_user_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    /**
     * Identificador único del usuario.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Nombre del usuario.
     *
     * <p>Campo obligatorio usado para personalización de la experiencia
     * de usuario y comunicaciones del sistema.</p>
     */
    @Column(name = "first_name", nullable = false, length = 250)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre no puede exceder 250 caracteres")
    private String firstName;

    /**
     * Apellido del usuario.
     *
     * <p>Campo opcional que complementa la identificación del usuario.
     * Útil para comunicaciones formales y verificación de identidad.</p>
     */
    @Column(name = "last_name", length = 250)
    @Size(max = 250, message = "El apellido no puede exceder 250 caracteres")
    private String lastName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Sirve como identificador único para autenticación y comunicaciones.
     * Debe ser válida y única en todo el sistema.</p>
     */
    @Column(name = "email", nullable = false, unique = true, length = 250)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    @Size(max = 250, message = "El email no puede exceder 250 caracteres")
    private String email;

    /**
     * Contraseña cifrada del usuario.
     *
     * <p>Almacenada usando un algoritmo de hash seguro (bcrypt).
     * No debe exponerse nunca en logs o respuestas de API.</p>
     */
    @Column(name = "password", nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    /**
     * Dirección física del usuario.
     *
     * <p>Campo opcional de texto libre para almacenar la dirección completa.
     * Útil para servicios a domicilio y verificación de ubicación para cuidadores.</p>
     */
    @Column(name = "address", columnDefinition = "TEXT")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

    /**
     * Número de teléfono del usuario.
     *
     * <p>Campo opcional para comunicaciones de emergencia y coordinación
     * de servicios. Se almacena como texto para soportar formatos internacionales.</p>
     */
    @Column(name = "phone_number", length = 250)
    @Size(max = 250, message = "El número de teléfono no puede exceder 250 caracteres")
    private String phoneNumber;

    /**
     * Rol del usuario en el sistema.
     *
     * <p>Define los permisos y funcionalidades disponibles para el usuario.
     * Utilizado por Spring Security para control de acceso basado en roles.</p>
     *
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @NotNull(message = "El rol del usuario es obligatorio")
    private Role role;

    /**
     * Indica si la cuenta del usuario está activa.
     *
     * <p>Los usuarios inactivos no pueden acceder al sistema ni realizar
     * operaciones. Se usa para deshabilitación temporal sin eliminación
     * de datos.</p>
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    @Builder.Default
    private boolean isActive = true;

    /**
     * Fecha y hora de verificación del email.
     *
     * <p>Campo opcional que se establece cuando el usuario verifica
     * su dirección de correo electrónico. Null indica email no verificado.</p>
     */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /**
     * Fecha y hora del último login exitoso.
     *
     * <p>Se actualiza cada vez que el usuario se autentica correctamente.
     * Útil para análisis de actividad y seguridad.</p>
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Fecha y hora de creación del usuario.
     *
     * <p>Se establece automáticamente al persistir la entidad.
     * Inmutable después de la creación.</p>
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     *
     * <p>Se actualiza automáticamente cada vez que se modifica la entidad.
     * Útil para auditoría y control de concurrencia.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Membresías del usuario en diferentes cuentas familiares.
     *
     * <p>Relación con la tabla intermedia que define las membresías
     * de este usuario en diferentes cuentas. Permite que un usuario
     * pueda gestionar mascotas de múltiples familias.</p>
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<AccountUser> accountMemberships = new HashSet<>();

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public User() {
        this.isActive = true;
    }

    /**
     * Constructor principal para crear un nuevo usuario.
     *
     * @param firstName nombre del usuario
     * @param lastName apellido del usuario
     * @param email dirección de correo electrónico
     * @param password contraseña cifrada
     * @param address dirección física
     * @param phoneNumber número de teléfono
     * @param role rol del usuario
     */
    public User(String firstName, String lastName, String email, String password,
                String address, String phoneNumber, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isActive = true;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Set<AccountUser> getAccountMemberships() {
        return accountMemberships;
    }

    public void setAccountMemberships(Set<AccountUser> accountMemberships) {
        this.accountMemberships = accountMemberships;
    }

    // ========== MÉTODOS SPRING SECURITY UserDetails ==========

    /**
     * Retorna las autoridades concedidas al usuario.
     *
     * <p>Implementación de Spring Security que convierte el rol del usuario
     * en una autoridad reconocida por el framework de seguridad.</p>
     *
     * @return colección con la autoridad basada en el rol del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Retorna el nombre de usuario utilizado para autenticación.
     *
     * <p>En este sistema, el email funciona como nombre de usuario único.</p>
     *
     * @return el email del usuario
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Indica si la cuenta del usuario está habilitada.
     *
     * <p>Los usuarios deshabilitados no pueden autenticarse en el sistema.</p>
     *
     * @return true si la cuenta está activa, false en caso contrario
     */
    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    /**
     * Indica si la cuenta del usuario no ha expirado.
     *
     * <p>En esta implementación, las cuentas no expiran automáticamente.</p>
     *
     * @return siempre true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta del usuario no está bloqueada.
     *
     * <p>En esta implementación, no se maneja bloqueo automático de cuentas.</p>
     *
     * @return siempre true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales del usuario no han expirado.
     *
     * <p>En esta implementación, las credenciales no expiran automáticamente.</p>
     *
     * @return siempre true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // ========== MÉTODOS UTILITARIOS ==========

    /**
     * Retorna el nombre completo del usuario.
     *
     * <p>Combina nombre y apellido, manejando casos donde el apellido
     * puede ser null o vacío.</p>
     *
     * @return nombre completo formateado
     */
    public String getFullName() {
        if (lastName == null || lastName.trim().isEmpty()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Verifica si el email del usuario ha sido verificado.
     *
     * @return true si el email está verificado, false en caso contrario
     */
    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    /**
     * Marca el email como verificado estableciendo la fecha actual.
     */
    public void markEmailAsVerified() {
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de último login a la fecha actual.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el email único.
     *
     * <p>Dos usuarios son iguales si tienen el mismo email,
     * ya que este campo es único en el sistema.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && user.id != null) {
            return Objects.equals(this.id, user.id);
        }

        // Si no tienen ID, comparar por email (único)
        return Objects.equals(getEmail(), user.getEmail());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getEmail());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>No incluye información sensible como la contraseña.
     * Útil para logs de auditoría y debugging.</p>
     */
    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', role=%s, isActive=%s, createdAt=%s}",
                id, email, role, isActive, createdAt);
    }
}