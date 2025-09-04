package com.Petcare.Petcare.Models.Account;

import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Payment.PaymentMethod;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una cuenta de cliente o familiar en la plataforma.
 *
 * <p>La cuenta es la entidad principal para la gestión financiera y de recursos compartidos,
 * como mascotas, métodos de pago, reservas y facturación. Un usuario principal (propietario)
 * está a cargo de la cuenta, y otros usuarios pueden ser invitados a participar con diferentes
 * niveles de permisos.</p>
 *
 * <p><strong>Relaciones principales:</strong></p>
 * <ul>
 *   <li>Tiene un propietario principal ({@link User})</li>
 *   <li>Puede tener múltiples usuarios asociados ({@link AccountUser})</li>
 *   <li>Posee múltiples mascotas ({@link Pet})</li>
 *   <li>Gestiona múltiples métodos de pago ({@link PaymentMethod})</li>
 *   <li>Contiene múltiples reservas ({@link Booking})</li>
 *   <li>Recibe múltiples facturas ({@link Invoice})</li>
 * </ul>
 *
 * <p><strong>Características de gestión:</strong></p>
 * <ul>
 *   <li>Saldo unificado para toda la familia</li>
 *   <li>Facturación centralizada</li>
 *   <li>Permisos granulares por usuario</li>
 *   <li>Gestión compartida de mascotas</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Las operaciones financieras y de permisos
 * se validan en el service layer, no en esta entidad.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see User
 * @see AccountUser
 * @see Pet
 * @see PaymentMethod
 * @see Booking
 * @see Invoice
 */
@Entity
@Table(name = "accounts",
        indexes = {
                @Index(name = "idx_account_owner_user_id", columnList = "owner_user_id", unique = true),
                @Index(name = "idx_account_number", columnList = "account_number", unique = true),
                @Index(name = "idx_account_active", columnList = "is_active"),
                @Index(name = "idx_account_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class Account {

    /**
     * Identificador único de la cuenta.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * El usuario propietario principal de la cuenta.
     *
     * <p>Este usuario tiene los permisos más altos sobre la cuenta y es el
     * responsable final de la facturación y gestión de la cuenta.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_account_owner_user"))
    @NotNull(message = "El usuario propietario es obligatorio")
    private User ownerUser;

    /**
     * Número único de cuenta para identificación externa.
     *
     * <p>Generado siguiendo un patrón específico (ej: ACC-2024-001234).
     * Se muestra al cliente en facturas y comunicaciones oficiales.</p>
     */
    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 50, message = "El número de cuenta no puede exceder 50 caracteres")
    private String accountNumber;

    /**
     * Nombre descriptivo de la cuenta.
     *
     * <p>Nombre personalizable que identifica la cuenta de manera amigable
     * (ej: "Familia Pérez", "Cuenta de Fido y Luna").</p>
     */
    @Column(name = "account_name", nullable = false, length = 100)
    @NotBlank(message = "El nombre de la cuenta es obligatorio")
    @Size(max = 100, message = "El nombre de la cuenta no puede exceder los 100 caracteres")
    private String accountName;

    /**
     * Saldo actual de la cuenta.
     *
     * <p>Puede ser positivo (crédito disponible) o negativo (deuda pendiente).
     * Se actualiza automáticamente a través de la entidad AccountTransaction
     * cuando se procesan pagos y se generan facturas.</p>
     */
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El saldo de la cuenta es obligatorio")
    @Digits(integer = 8, fraction = 2, message = "El saldo debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Código de moneda de 3 letras según ISO 4217.
     *
     * <p>Define la moneda base para todas las transacciones de esta cuenta.
     * Ejemplos: "USD", "CLP", "EUR".</p>
     */
    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres")
    @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código ISO 4217 válido")
    private String currency = "USD";

    /**
     * Indica si la cuenta está activa o ha sido suspendida.
     *
     * <p>Las cuentas inactivas no pueden realizar nuevas reservas ni
     * procesar pagos, pero mantienen acceso a su historial.</p>
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Fecha y hora de creación de la cuenta.
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
     * Lista de usuarios asociados a esta cuenta.
     *
     * <p>Incluye tanto el propietario como usuarios adicionales invitados
     * con diferentes niveles de permisos (gestión de mascotas, pagos, reservas).</p>
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AccountUser> accountUsers = new HashSet<>();

    /**
     * Lista de mascotas que pertenecen a esta cuenta.
     *
     * <p>Todas las mascotas de la familia se centralizan bajo una cuenta,
     * permitiendo gestión compartida entre usuarios autorizados.</p>
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Pet> pets = new HashSet<>();

    /**
     * Métodos de pago registrados para esta cuenta.
     *
     * <p>Tarjetas de crédito/débito y otros métodos de pago disponibles
     * para procesar los pagos de las facturas de esta cuenta.</p>
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PaymentMethod> paymentMethods = new HashSet<>();

    /**
     * Reservas realizadas por esta cuenta.
     *
     * <p>Historial completo de todas las reservas de servicios realizadas
     * por cualquier usuario autorizado de esta cuenta.</p>
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    /**
     * Facturas emitidas a esta cuenta.
     *
     * <p>Todas las facturas generadas por servicios completados, incluyendo
     * facturas pagadas, pendientes y vencidas.</p>
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Invoice> invoices = new HashSet<>();

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public Account() {
    }

    /**
     * Constructor principal para crear una nueva cuenta.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas y la generación del número de cuenta se manejan en el service layer.</p>
     *
     * @param ownerUser el usuario propietario de la cuenta
     * @param accountName nombre descriptivo de la cuenta
     * @param accountNumber número único de identificación
     */
    public Account(User ownerUser, String accountName, String accountNumber) {
        this.ownerUser = ownerUser;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
    }

    /**
     * Constructor completo con todos los campos principales.
     * Principalmente para uso interno y testing.
     */
    public Account(Long id, User ownerUser, String accountNumber, String accountName,
                   BigDecimal balance, String currency, boolean isActive,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerUser = ownerUser;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.currency = currency;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public Set<AccountUser> getAccountUsers() {
        return accountUsers;
    }

    public void setAccountUsers(Set<AccountUser> accountUsers) {
        this.accountUsers = accountUsers;
    }

    public Set<Pet> getPets() {
        return pets;
    }

    public void setPets(Set<Pet> pets) {
        this.pets = pets;
    }

    public Set<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(Set<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    // ========== MÉTODOS DE UTILIDAD PARA RELACIONES BIDIRECCIONALES ==========

    /**
     * Agrega un usuario a esta cuenta manteniendo la consistencia bidireccional.
     */
    public void addUser(AccountUser user) {
        accountUsers.add(user);
        user.setAccount(this);
    }

    /**
     * Remueve un usuario de esta cuenta manteniendo la consistencia bidireccional.
     */
    public void removeUser(AccountUser user) {
        accountUsers.remove(user);
        user.setAccount(null);
    }

    /**
     * Agrega una mascota a esta cuenta manteniendo la consistencia bidireccional.
     */
    public void addPet(Pet pet) {
        pets.add(pet);
        pet.setAccount(this);
    }

    /**
     * Remueve una mascota de esta cuenta manteniendo la consistencia bidireccional.
     */
    public void removePet(Pet pet) {
        pets.remove(pet);
        pet.setAccount(null);
    }

    /**
     * Agrega un método de pago a esta cuenta manteniendo la consistencia bidireccional.
     */
    public void addPaymentMethod(PaymentMethod paymentMethod) {
        paymentMethods.add(paymentMethod);
        paymentMethod.setAccount(this);
    }

    /**
     * Remueve un método de pago de esta cuenta manteniendo la consistencia bidireccional.
     */
    public void removePaymentMethod(PaymentMethod paymentMethod) {
        paymentMethods.remove(paymentMethod);
        paymentMethod.setAccount(null);
    }

    /**
     * Agrega una reserva a esta cuenta manteniendo la consistencia bidireccional.
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setAccount(this);
    }

    /**
     * Remueve una reserva de esta cuenta manteniendo la consistencia bidireccional.
     */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setAccount(null);
    }

    /**
     * Agrega una factura a esta cuenta manteniendo la consistencia bidireccional.
     */
    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.setAccount(this);
    }

    /**
     * Remueve una factura de esta cuenta manteniendo la consistencia bidireccional.
     */
    public void removeInvoice(Invoice invoice) {
        invoices.remove(invoice);
        invoice.setAccount(null);
    }

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Verifica si la cuenta puede realizar nuevas reservas.
     *
     * @return true si la cuenta está activa
     */
    public boolean canMakeBookings() {
        return this.isActive;
    }

    /**
     * Verifica si la cuenta tiene saldo positivo.
     *
     * @return true si el saldo es mayor a cero
     */
    public boolean hasPositiveBalance() {
        return this.balance != null && this.balance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Verifica si la cuenta tiene deuda pendiente.
     *
     * @return true si el saldo es negativo
     */
    public boolean hasDebt() {
        return this.balance != null && this.balance.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Obtiene el método de pago por defecto de la cuenta.
     *
     * @return el PaymentMethod marcado como default, o null si no existe
     */
    public PaymentMethod getDefaultPaymentMethod() {
        return paymentMethods.stream()
                .filter(PaymentMethod::isDefault)
                .findFirst()
                .orElse(null);
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el ID único y número de cuenta.
     *
     * <p>Se incluye el número de cuenta como campo de negocio único para garantizar
     * consistencia en colecciones hash incluso durante el ciclo de vida de la entidad.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && account.id != null) {
            return Objects.equals(this.id, account.id);
        }

        // Si no tienen ID, comparar por número de cuenta único
        return Objects.equals(getAccountNumber(), account.getAccountNumber());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getAccountNumber());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y problemas de rendimiento.</p>
     */
    @Override
    public String toString() {
        return String.format("Account{id=%d, accountNumber='%s', accountName='%s', balance=%s, isActive=%s, createdAt=%s}",
                id, accountNumber, accountName, balance, isActive, createdAt);
    }
}