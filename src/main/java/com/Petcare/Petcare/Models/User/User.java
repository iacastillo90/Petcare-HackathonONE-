package com.Petcare.Petcare.Models.User;

import com.Petcare.Petcare.Models.Account.AccountUser;
import com.Petcare.Petcare.Models.SitterProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
 * como cuidadores de mascotas, proporcionando la base para todo el sistema de
 * gestión de usuarios y permisos.</p>
 *
 * <p><strong>Roles principales:</strong></p>
 * <ul>
 * <li>CLIENT: Usuario propietario de mascotas que solicita servicios</li>
 * <li>SITTER: Usuario cuidador que ofrece servicios de cuidado</li>
 * <li>ADMIN: Usuario administrador del sistema</li>
 * </ul>
 *
 * <p><strong>Características de seguridad:</strong></p>
 * <ul>
 * <li>Integración completa con Spring Security</li>
 * <li>Control de estado de cuenta (activo/inactivo)</li>
 * <li>Verificación de email obligatoria</li>
 * <li>Seguimiento de actividad y último login</li>
 * <li>Contraseñas hasheadas con bcrypt</li>
 * </ul>
 *
 * <p><strong>Membresías multi-cuenta:</strong></p>
 * <p>Un usuario puede pertenecer a múltiples cuentas familiares a través
 * de la entidad intermedia {@link AccountUser}, permitiendo gestión
 * colaborativa de mascotas con permisos granulares.</p>
 *
 * <p><strong>Perfil de cuidador:</strong></p>
 * <p>Los usuarios con rol SITTER pueden tener un perfil de cuidador asociado
 * a través de la relación {@link SitterProfile}, que contiene información
 * específica del servicio de cuidado como tarifas y disponibilidad.</p>
 *
 * <p><strong>Nota:</strong> Las validaciones de negocio complejas, cifrado
 * de contraseñas y lógica de autenticación se manejan en el service layer.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see UserDetails
 * @see Role
 * @see AccountUser
 * @see SitterProfile
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true),
                @Index(name = "idx_user_role", columnList = "role"),
                @Index(name = "idx_user_active", columnList = "is_active"),
                @Index(name = "idx_user_permission_level", columnList = "permission_level"),
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
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String firstName;

    /**
     * Apellido del usuario.
     *
     * <p>Campo obligatorio que complementa la identificación del usuario.
     * Útil para comunicaciones formales y verificación de identidad.</p>
     */
    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String lastName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Sirve como identificador único para autenticación y comunicaciones.
     * Debe ser válida y única en todo el sistema.</p>
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    /**
     * Contraseña cifrada del usuario.
     *
     * <p>Almacenada usando bcrypt. Nunca debe exponerse en logs,
     * respuestas de API o ser enviada al frontend.</p>
     */
    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255, message = "La contraseña cifrada no puede exceder 255 caracteres")
    private String password;

    /**
     * Número de teléfono del usuario.
     *
     * <p>Campo obligatorio para comunicaciones de emergencia y coordinación
     * de servicios. Se almacena como texto para soportar formatos internacionales.</p>
     */
    @Column(name = "phone_number", nullable = false, length = 20)
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    private String phoneNumber;

    /**
     * Dirección física del usuario.
     *
     * <p>Campo obligatorio para servicios a domicilio y verificación
     * de ubicación para cuidadores.</p>
     */
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

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
     * Nivel de permisos adicional del usuario.
     *
     * <p>Proporciona una capa adicional de control de acceso más granular
     * que complementa el rol básico del usuario.</p>
     *
     * @see PermissionLevel
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false, length = 20)
    @NotNull(message = "El nivel de permisos es obligatorio")
    private PermissionLevel permissionLevel = PermissionLevel.BASIC;

    /**
     * Indica si la cuenta del usuario está activa.
     *
     * <p>Los usuarios inactivos no pueden acceder al sistema ni realizar
     * operaciones. Se usa para deshabilitación temporal sin eliminación
     * de datos.</p>
     */
    @Column(name = "is_active", nullable = false)
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
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AccountUser> accountMemberships = new HashSet<>();

    /**
     * Perfil de cuidador asociado al usuario.
     *
     * <p>Relación opcional que existe únicamente para usuarios con rol SITTER.
     * Contiene información específica del servicio de cuidado como tarifas,
     * disponibilidad y calificaciones.</p>
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SitterProfile sitterProfile;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public User() {
    }

    /**
     * Constructor principal para crear un nuevo usuario.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas y el cifrado de contraseña se manejan en el service layer.</p>
     *
     * @param firstName nombre del usuario
     * @param lastName apellido del usuario
     * @param email dirección de correo electrónico única
     * @param password contraseña ya cifrada
     * @param phoneNumber número de teléfono
     * @param address dirección física
     * @param role rol del usuario en el sistema
     */
    public User(String firstName, String lastName, String email, String password,
                String phoneNumber, String address, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
    }

    /**
     * Constructor completo con todos los campos principales.
     * Principalmente para uso interno y testing.
     */
    public User(Long id, String firstName, String lastName, String email, String password,
                String phoneNumber, String address, Role role, PermissionLevel permissionLevel,
                boolean isActive, LocalDateTime emailVerifiedAt, LocalDateTime lastLoginAt,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.permissionLevel = permissionLevel;
        this.isActive = isActive;
        this.emailVerifiedAt = emailVerifiedAt;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el identificador único del usuario.
     * @return el ID único del usuario.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el identificador único del usuario.
     * <p>Nota: Normalmente gestionado por JPA.</p>
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
     * @param firstName el nuevo nombre para el usuario.
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
     * @param lastName el nuevo apellido para el usuario.
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
     * Obtiene la contraseña cifrada del usuario.
     * @return la contraseña cifrada.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña cifrada del usuario.
     * @param password la nueva contraseña cifrada.
     */
    public void setPassword(String password) {
        this.password = password;
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
     * Obtiene la dirección física del usuario.
     * @return la dirección del usuario.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Establece la dirección física del usuario.
     * @param address la nueva dirección del usuario.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Obtiene el rol principal del usuario en el sistema.
     * @return el rol del usuario.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Establece el rol principal del usuario en el sistema.
     * @param role el nuevo rol para el usuario.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Obtiene el nivel de permisos granular del usuario.
     * @return el nivel de permiso.
     */
    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * Establece el nivel de permisos granular del usuario.
     * @param permissionLevel el nuevo nivel de permiso.
     */
    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    /**
     * Verifica si la cuenta del usuario está activa.
     * @return {@code true} si está activa, {@code false} en caso contrario.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Establece el estado de activación de la cuenta del usuario.
     * @param active {@code true} para activar, {@code false} para desactivar.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Obtiene la fecha y hora en que se verificó el email del usuario.
     * @return el timestamp de verificación, o {@code null} si no ha sido verificado.
     */
    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    /**
     * Establece la fecha y hora de verificación del email.
     * @param emailVerifiedAt el timestamp de verificación.
     */
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    /**
     * Obtiene la fecha y hora del último inicio de sesión exitoso.
     * @return el timestamp del último login.
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Establece la fecha y hora del último inicio de sesión exitoso.
     * @param lastLoginAt el timestamp del último login.
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * Obtiene la fecha y hora de creación del registro del usuario.
     * <p>Nota: Gestionado automáticamente por JPA Auditing.</p>
     * @return el timestamp de creación.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece la fecha y hora de creación del registro del usuario.
     * <p>Nota: Usar solo en casos específicos como migraciones de datos.</p>
     * @param createdAt el timestamp de creación.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtiene la fecha y hora de la última actualización del registro.
     * <p>Nota: Gestionado automáticamente por JPA Auditing.</p>
     * @return el timestamp de la última actualización.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Establece la fecha y hora de la última actualización del registro.
     * <p>Nota: Usar solo en casos específicos como migraciones de datos.</p>
     * @param updatedAt el timestamp de la última actualización.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Obtiene el conjunto de membresías de cuenta a las que pertenece este usuario.
     * @return un {@code Set} de {@link AccountUser}.
     */
    public Set<AccountUser> getAccountMemberships() {
        return accountMemberships;
    }

    /**
     * Establece el conjunto de membresías de cuenta para este usuario.
     * @param accountMemberships el nuevo conjunto de membresías.
     */
    public void setAccountMemberships(Set<AccountUser> accountMemberships) {
        this.accountMemberships = accountMemberships;
    }

    /**
     * Obtiene el perfil de cuidador asociado a este usuario.
     * @return el {@link SitterProfile}, o {@code null} si el usuario no es cuidador.
     */
    public SitterProfile getSitterProfile() {
        return sitterProfile;
    }

    /**
     * Establece o asocia un perfil de cuidador a este usuario.
     * @param sitterProfile el perfil de cuidador a asociar.
     */
    public void setSitterProfile(SitterProfile sitterProfile) {
        this.sitterProfile = sitterProfile;
    }

    // ========== MÉTODOS DE UTILIDAD PARA RELACIONES BIDIRECCIONALES ==========

    /**
     * Agrega una membresía de cuenta a este usuario, asegurando la consistencia
     * de la relación bidireccional.
     *
     * @param accountUser la membresía {@link AccountUser} a agregar.
     */
    public void addAccountMembership(AccountUser accountUser) {
        accountMemberships.add(accountUser);
        accountUser.setUser(this);
    }

    /**
     * Remueve una membresía de cuenta de este usuario, asegurando la consistencia
     * de la relación bidireccional.
     *
     * @param accountUser la membresía {@link AccountUser} a remover.
     */
    public void removeAccountMembership(AccountUser accountUser) {
        accountMemberships.remove(accountUser);
        accountUser.setUser(null);
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

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Retorna el nombre completo del usuario concatenando nombre y apellido.
     *
     * @return una cadena con el nombre completo del usuario.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Verifica si el email del usuario ha sido verificado.
     *
     * @return {@code true} si el email está verificado (timestamp no es nulo), {@code false} en caso contrario.
     */
    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    /**
     * Marca el email como verificado estableciendo el timestamp actual.
     * Este método debe ser llamado por el servicio de negocio tras una verificación exitosa.
     */
    public void markEmailAsVerified() {
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de último login al timestamp actual.
     * Este método es llamado por el servicio de autenticación tras un login exitoso.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Verifica si el usuario es un cuidador.
     *
     * @return {@code true} si el usuario tiene el rol {@code SITTER}.
     */
    public boolean isSitter() {
        return Role.SITTER.equals(this.role);
    }

    /**
     * Verifica si el usuario es un cliente.
     *
     * @return {@code true} si el usuario tiene el rol {@code CLIENT}.
     */
    public boolean isClient() {
        return Role.CLIENT.equals(this.role);
    }

    /**
     * Verifica si el usuario es un administrador.
     *
     * @return {@code true} si el usuario tiene el rol {@code ADMIN}.
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    /**
     * Verifica si el usuario tiene un perfil de cuidador asociado.
     *
     * @return {@code true} si el campo {@code sitterProfile} no es nulo.
     */
    public boolean hasSitterProfile() {
        return sitterProfile != null;
    }

    /**
     * Verifica si el usuario puede realizar operaciones como cuidador.
     *
     * @return {@code true} si es {@code SITTER}, tiene perfil activo y la cuenta está activa.
     */
    public boolean canProvideSitterServices() {
        return isSitter() && hasSitterProfile() && isActive;
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el ID único y email.
     *
     * <p>Se incluye el email como campo de negocio único para garantizar
     * consistencia en colecciones hash incluso durante el ciclo de vida de la entidad.</p>
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

        // Si no tienen ID, comparar por email único
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
        return String.format("User{id=%d, email='%s', fullName='%s', role=%s, isActive=%s, createdAt=%s}",
                id, email, getFullName(), role, isActive, createdAt);
    }
}