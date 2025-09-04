package com.Petcare.Petcare.Models.Account;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad intermedia que representa la membresía de un Usuario en una Cuenta.
 *
 * <p>Esta entidad implementa la relación Muchos a Muchos entre Cuentas y Usuarios,
 * permitiendo que múltiples usuarios compartan una cuenta familiar con diferentes
 * niveles de permisos y responsabilidades. Es fundamental para el control de acceso
 * granular en el sistema Petcare.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 * <li>Gestión de membresías familiares: múltiples usuarios por cuenta</li>
 * <li>Control de permisos granulares por usuario y cuenta</li>
 * <li>Asignación de roles específicos dentro del contexto de cuenta</li>
 * <li>Auditoría de cambios en membresías y permisos</li>
 * <li>Gestión de acceso temporal y revocación de permisos</li>
 * </ul>
 *
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 * <li>Agregar miembros familiares a una cuenta existente</li>
 * <li>Asignar permisos específicos a usuarios (ej: solo reservas, no pagos)</li>
 * <li>Gestión de acceso temporal para cuidadores familiares</li>
 * <li>Control parental sobre acciones de menores de edad</li>
 * <li>Delegación de responsabilidades administrativas</li>
 * </ul>
 *
 * <p><strong>Niveles de permisos típicos:</strong></p>
 * <ul>
 * <li>OWNER: Todos los permisos habilitados</li>
 * <li>ADMIN: Gestión de mascotas y reservas, pagos según configuración</li>
 * <li>MEMBER: Solo reservas y gestión básica de mascotas</li>
 * <li>VIEWER: Solo consulta de información</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 * <li>Entidad de asociación con atributos adicionales</li>
 * <li>Constraint único para prevenir membresías duplicadas</li>
 * <li>Validación de integridad referencial</li>
 * <li>Auditoría automática de cambios</li>
 * <li>Lazy loading para optimización de performance</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Account
 * @see User
 * @see Role
 */
@Entity
@Table(name = "account_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_user", columnNames = {"account_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_account_user_account_id", columnList = "account_id"),
                @Index(name = "idx_account_user_user_id", columnList = "user_id"),
                @Index(name = "idx_account_user_role", columnList = "role"),
                @Index(name = "idx_account_user_permissions", columnList = "can_manage_payments, can_manage_pets, can_make_bookings"),
                @Index(name = "idx_account_user_added_at", columnList = "added_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class AccountUser {

    /**
     * Identificador único de la membresía.
     * <p>Generado automáticamente por la base de datos.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cuenta a la cual pertenece esta membresía.
     *
     * <p>Relación obligatoria que establece la cuenta familiar o empresarial
     * a la cual el usuario tiene acceso. La carga es lazy para optimizar
     * el rendimiento en consultas masivas.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accountuser_account"))
    @NotNull(message = "La cuenta es obligatoria para la membresía")
    private Account account;

    /**
     * Usuario que es miembro de la cuenta.
     *
     * <p>Relación obligatoria que identifica al usuario específico que tiene
     * acceso a la cuenta. Un usuario puede ser miembro de múltiples cuentas
     * con diferentes roles y permisos.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accountuser_user"))
    @NotNull(message = "El usuario es obligatorio para la membresía")
    private User user;

    /**
     * Rol del usuario dentro del contexto de esta cuenta específica.
     *
     * <p>Define el nivel general de responsabilidad del usuario en la cuenta.
     * Este rol puede diferir del rol global del usuario en el sistema.
     * Por ejemplo, un ADMIN global puede ser solo MEMBER en una cuenta específica.</p>
     *
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El rol es obligatorio para la membresía")
    private Role role;

    /**
     * Permiso para gestionar métodos de pago y transacciones financieras.
     *
     * <p>Controla si el usuario puede agregar/remover tarjetas de crédito,
     * procesar pagos, ver historial financiero y gestionar saldos de cuenta.
     * Es crítico para la seguridad financiera.</p>
     */
    @Column(name = "can_manage_payments", nullable = false)
    private boolean canManagePayments = false;

    /**
     * Permiso para gestionar mascotas de la cuenta.
     *
     * <p>Controla si el usuario puede agregar, editar o desactivar mascotas,
     * modificar información médica y notas especiales. Fundamental para
     * la gestión del cuidado animal.</p>
     */
    @Column(name = "can_manage_pets", nullable = false)
    private boolean canManagePets = false;

    /**
     * Permiso para crear y gestionar reservas de servicios.
     *
     * <p>Controla si el usuario puede crear nuevas reservas, cancelar existentes
     * y modificar detalles de servicios. Por defecto habilitado para facilitar
     * el uso básico de la plataforma.</p>
     */
    @Column(name = "can_make_bookings", nullable = false)
    private boolean canMakeBookings = true;

    /**
     * Fecha y hora en que el usuario fue agregado a la cuenta.
     *
     * <p>Registro de auditoría que permite rastrear cuándo se estableció
     * la membresía. Útil para análisis de crecimiento y resolución de disputas.</p>
     */
    @CreatedDate
    @Column(name = "added_at", updatable = false, nullable = false)
    private LocalDateTime addedAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido por JPA.
     */
    public AccountUser() {}

    /**
     * Constructor para crear una nueva membresía con rol específico.
     *
     * <p>Los permisos se establecen en valores por defecto seguros que pueden
     * ser modificados posteriormente según las necesidades específicas.</p>
     *
     * @param account la cuenta a la cual se agrega el usuario
     * @param user el usuario que se convierte en miembro
     * @param role el rol inicial del usuario en la cuenta
     */
    public AccountUser(Account account, User user, Role role) {
        this.account = account;
        this.user = user;
        this.role = role;
    }

    /**
     * Constructor completo con permisos específicos.
     *
     * <p>Permite establecer todos los permisos de manera explícita
     * al crear la membresía.</p>
     *
     * @param account la cuenta a la cual se agrega el usuario
     * @param user el usuario que se convierte en miembro
     * @param role el rol del usuario en la cuenta
     * @param canManagePayments permiso para gestionar pagos
     * @param canManagePets permiso para gestionar mascotas
     * @param canMakeBookings permiso para crear reservas
     */
    public AccountUser(Account account, User user, Role role,
                       boolean canManagePayments, boolean canManagePets, boolean canMakeBookings) {
        this.account = account;
        this.user = user;
        this.role = role;
        this.canManagePayments = canManagePayments;
        this.canManagePets = canManagePets;
        this.canMakeBookings = canMakeBookings;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el identificador único de la membresía.
     *
     * @return el ID único de la relación account-user
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el identificador único de la membresía.
     *
     * <p>Normalmente gestionado automáticamente por JPA.
     * Solo debe establecerse manualmente en casos excepcionales.</p>
     *
     * @param id el nuevo ID de la membresía
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene la cuenta asociada a esta membresía.
     *
     * @return la cuenta de la cual el usuario es miembro
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Establece la cuenta asociada a esta membresía.
     *
     * <p>Cambiar la cuenta de una membresía existente puede tener
     * implicaciones significativas en permisos y acceso a datos.</p>
     *
     * @param account la nueva cuenta a asociar
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Obtiene el usuario miembro de la cuenta.
     *
     * @return el usuario que tiene acceso a la cuenta
     */
    public User getUser() {
        return user;
    }

    /**
     * Establece el usuario miembro de la cuenta.
     *
     * <p>Cambiar el usuario de una membresía existente puede requerir
     * validaciones adicionales de seguridad y auditoría.</p>
     *
     * @param user el nuevo usuario a asociar con la cuenta
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Obtiene el rol del usuario dentro de la cuenta.
     *
     * @return el rol asignado al usuario en esta cuenta específica
     */
    public Role getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario dentro de la cuenta.
     *
     * <p>Los cambios de rol pueden afectar automáticamente los permisos
     * granulares según las reglas de negocio implementadas en el service layer.</p>
     *
     * @param role el nuevo rol a asignar al usuario
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Verifica si el usuario puede gestionar métodos de pago y transacciones.
     *
     * @return true si el usuario tiene permisos financieros en esta cuenta
     */
    public boolean isCanManagePayments() {
        return canManagePayments;
    }

    /**
     * Establece el permiso para gestionar métodos de pago y transacciones.
     *
     * <p>Este permiso es crítico para la seguridad financiera. Los cambios
     * deberían ser auditados y requerir autenticación adicional.</p>
     *
     * @param canManagePayments true para otorgar permisos financieros
     */
    public void setCanManagePayments(boolean canManagePayments) {
        this.canManagePayments = canManagePayments;
    }

    /**
     * Verifica si el usuario puede gestionar mascotas de la cuenta.
     *
     * @return true si el usuario puede agregar, editar o desactivar mascotas
     */
    public boolean isCanManagePets() {
        return canManagePets;
    }

    /**
     * Establece el permiso para gestionar mascotas de la cuenta.
     *
     * <p>Incluye la capacidad de modificar información médica crítica
     * y notas especiales que afectan la seguridad y bienestar animal.</p>
     *
     * @param canManagePets true para otorgar permisos de gestión de mascotas
     */
    public void setCanManagePets(boolean canManagePets) {
        this.canManagePets = canManagePets;
    }

    /**
     * Verifica si el usuario puede crear y gestionar reservas.
     *
     * @return true si el usuario puede solicitar servicios de cuidado
     */
    public boolean isCanMakeBookings() {
        return canMakeBookings;
    }

    /**
     * Establece el permiso para crear y gestionar reservas.
     *
     * <p>Permiso básico que permite al usuario solicitar servicios
     * de cuidado para las mascotas de la cuenta. Generalmente habilitado
     * por defecto para facilitar el uso de la plataforma.</p>
     *
     * @param canMakeBookings true para otorgar permisos de reserva
     */
    public void setCanMakeBookings(boolean canMakeBookings) {
        this.canMakeBookings = canMakeBookings;
    }

    /**
     * Obtiene la fecha y hora de incorporación a la cuenta.
     *
     * @return timestamp de cuando se estableció la membresía
     */
    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    /**
     * Establece la fecha y hora de incorporación a la cuenta.
     *
     * <p>Normalmente gestionado automáticamente por JPA auditing.
     * Solo debe modificarse manualmente en casos de migración de datos.</p>
     *
     * @param addedAt la fecha y hora de incorporación
     */
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Verifica si el usuario tiene permisos completos en la cuenta.
     *
     * <p>Un usuario tiene permisos completos si puede gestionar pagos,
     * mascotas y hacer reservas. Típicamente corresponde al propietario
     * o administradores principales de la cuenta.</p>
     *
     * @return true si tiene todos los permisos habilitados
     */
    public boolean hasFullPermissions() {
        return canManagePayments && canManagePets && canMakeBookings;
    }

    /**
     * Verifica si el usuario tiene al menos un permiso habilitado.
     *
     * <p>Útil para validar que la membresía tiene propósito. Una membresía
     * sin permisos efectivamente bloquea el acceso del usuario.</p>
     *
     * @return true si tiene al menos un permiso habilitado
     */
    public boolean hasAnyPermission() {
        return canManagePayments || canManagePets || canMakeBookings;
    }

    /**
     * Verifica si es el propietario de la cuenta.
     *
     * <p>El propietario típicamente es el usuario principal que creó la cuenta
     * y tiene todos los permisos habilitados por defecto.</p>
     *
     * @return true si es propietario según rol y permisos
     */
    public boolean isAccountOwner() {
        return Role.ADMIN.equals(role) && hasFullPermissions();
    }

    /**
     * Aplica permisos predeterminados basados en el rol asignado.
     *
     * <p>Método de conveniencia para establecer permisos típicos
     * según el rol. Puede ser sobrescrito posteriormente si se requieren
     * configuraciones específicas.</p>
     */
    public void applyDefaultPermissionsForRole() {
        switch (role) {
            case ADMIN:
                this.canManagePayments = true;
                this.canManagePets = true;
                this.canMakeBookings = true;
                break;
            case CLIENT:
                this.canManagePayments = false;
                this.canManagePets = true;
                this.canMakeBookings = true;
                break;
            case SITTER:
                this.canManagePayments = false;
                this.canManagePets = false;
                this.canMakeBookings = false;
                break;
            default:
                this.canManagePayments = false;
                this.canManagePets = false;
                this.canMakeBookings = false;
                break;
        }
    }

    /**
     * Obtiene una descripción textual de los permisos activos.
     *
     * @return cadena describiendo los permisos habilitados
     */
    public String getPermissionsDescription() {
        if (!hasAnyPermission()) {
            return "Sin permisos";
        }

        StringBuilder permissions = new StringBuilder();
        if (canManagePayments) permissions.append("Pagos, ");
        if (canManagePets) permissions.append("Mascotas, ");
        if (canMakeBookings) permissions.append("Reservas, ");

        // Remover la última coma y espacio
        String result = permissions.toString();
        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    /**
     * Valida si el usuario puede realizar una acción específica.
     *
     * @param action la acción que se desea validar
     * @return true si el usuario tiene los permisos necesarios
     */
    public boolean canPerform(String action) {
        switch (action.toLowerCase()) {
            case "manage_payments":
            case "payment":
                return canManagePayments;
            case "manage_pets":
            case "pets":
                return canManagePets;
            case "make_bookings":
            case "booking":
                return canMakeBookings;
            default:
                return false;
        }
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en la combinación única de cuenta y usuario.
     *
     * <p>Dos membresías son iguales si involucran la misma cuenta y el mismo usuario,
     * independientemente de roles o permisos que pueden cambiar con el tiempo.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountUser)) return false;
        AccountUser that = (AccountUser) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // Si no tienen ID, comparar por la combinación única de account y user
        return Objects.equals(getAccount(), that.getAccount()) &&
                Objects.equals(getUser(), that.getUser());
    }

    /**
     * Implementación de hashCode consistente con equals.
     *
     * <p>Basado en la combinación única de cuenta y usuario para
     * garantizar comportamiento correcto en colecciones hash.</p>
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getAccount(), getUser());
    }

    /**
     * Representación de cadena optimizada para logging y debugging.
     *
     * <p>Evita lazy loading exceptions al no acceder directamente a las
     * entidades relacionadas, mostrando solo los IDs relevantes.</p>
     */
    @Override
    public String toString() {
        return String.format("AccountUser{id=%d, accountId=%d, userId=%d, role=%s, " +
                        "permissions=[payments=%s, pets=%s, bookings=%s], addedAt=%s}",
                id,
                (account != null ? account.getId() : null),
                (user != null ? user.getId() : null),
                role,
                canManagePayments, canManagePets, canMakeBookings,
                addedAt);
    }
}