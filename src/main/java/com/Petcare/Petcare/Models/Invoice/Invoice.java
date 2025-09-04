package com.Petcare.Petcare.Models.Invoice;

import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Payment.Payment;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una factura generada por los servicios de cuidado de mascotas.
 *
 * <p>Esta entidad documenta formalmente los cargos asociados a una reserva completada,
 * proporcionando un registro detallado de la facturación y el estado de pagos
 * para auditoría financiera y transparencia con el cliente.</p>
 *
 * <p><strong>Relaciones principales:</strong></p>
 * <ul>
 *   <li>Pertenece a una cuenta específica ({@link Account})</li>
 *   <li>Basada en una reserva completada ({@link Booking})</li>
 *   <li>Puede contener múltiples items de facturación ({@link InvoiceItem})</li>
 *   <li>Puede tener múltiples pagos asociados ({@link Payment})</li>
 * </ul>
 *
 * <p><strong>Estados posibles:</strong></p>
 * <ul>
 *   <li>DRAFT: Factura en borrador, no enviada al cliente</li>
 *   <li>SENT: Enviada al cliente, pendiente de pago</li>
 *   <li>PAID: Pagada completamente</li>
 *   <li>PARTIALLY_PAID: Pagada parcialmente</li>
 *   <li>OVERDUE: Vencida, pago no recibido</li>
 *   <li>CANCELLED: Cancelada, no requiere pago</li>
 *   <li>REFUNDED: Reembolsada total o parcialmente</li>
 * </ul>
 *
 * <p><strong>Flujo de negocio:</strong></p>
 * <ol>
 *   <li>Reserva se completa (BOOKING.COMPLETED)</li>
 *   <li>Se genera factura automáticamente (DRAFT)</li>
 *   <li>Se envía al cliente (SENT)</li>
 *   <li>Cliente realiza pago (PAID)</li>
 *   <li>Se procesa pago y se actualiza estado</li>
 * </ol>
 *
 * <p><strong>Nota:</strong> Los cálculos de totales y validaciones de negocio
 * se manejan en el service layer, no en esta entidad.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Account
 * @see Booking
 * @see InvoiceItem
 * @see Payment
 * @see InvoiceStatus
 */
@Entity
@Table(name = "invoices",
        indexes = {
                @Index(name = "idx_invoice_number", columnList = "invoice_number", unique = true),
                @Index(name = "idx_invoice_account", columnList = "account_id"),
                @Index(name = "idx_invoice_booking", columnList = "booking_id"),
                @Index(name = "idx_invoice_status", columnList = "status"),
                @Index(name = "idx_invoice_due_date", columnList = "due_date"),
                @Index(name = "idx_invoice_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class Invoice {

    /**
     * Identificador único de la factura.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cuenta a la cual pertenece esta factura.
     *
     * <p>Relación obligatoria que establece qué cuenta será facturada.
     * La carga es lazy para optimizar el rendimiento.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoice_account"))
    private Account account;

    /**
     * Reserva que origina esta factura.
     *
     * <p>Relación obligatoria que vincula la factura con el servicio prestado.
     * Cada factura debe estar basada en una reserva completada.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoice_booking"))
    private Booking booking;

    /**
     * Número único de factura para identificación externa.
     *
     * <p>Generado siguiendo un patrón específico (ej: INV-2024-001234).
     * Debe ser único en todo el sistema y se muestra al cliente.</p>
     */
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    /**
     * Fecha de emisión de la factura.
     *
     * <p>Representa el momento en que se generó la factura.
     * Normalmente coincide con la fecha de completación del servicio.</p>
     */
    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    /**
     * Fecha límite para el pago de la factura.
     *
     * <p>Calculada basándose en los términos de pago de la cuenta.
     * Después de esta fecha, la factura se considera vencida.</p>
     */
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    /**
     * Subtotal antes de aplicar la tarifa de plataforma y impuestos.
     *
     * <p>Suma de todos los items de la factura antes de cargos adicionales.
     * Se calcula automáticamente basándose en los InvoiceItems.</p>
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Tarifa de la plataforma aplicada a esta factura.
     *
     * <p>Comisión que cobra Petcare por el uso de la plataforma.
     * Se calcula como un porcentaje del subtotal.</p>
     */
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;

    /**
     * Monto total de la factura incluidos todos los cargos.
     *
     * <p>Suma final que debe pagar el cliente: subtotal + platformFee.
     * Este es el monto que aparece en los métodos de pago.</p>
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Estado actual de la factura.
     *
     * <p>Controla el flujo de facturación y determina las acciones disponibles.
     * Los cambios de estado se gestionan en el service layer.</p>
     *
     * @see InvoiceStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    /**
     * Notas adicionales sobre la factura.
     *
     * <p>Campo de texto libre para información adicional, términos especiales,
     * o cualquier observación relevante que debe aparecer en la factura.</p>
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Fecha y hora de creación de la factura.
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
     * Items detallados de la factura.
     *
     * <p>Lista de productos o servicios incluidos en esta factura.
     * Cada item tiene su propia descripción, cantidad y precio.</p>
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<InvoiceItem> invoiceItems = new HashSet<>();

    /**
     * Pagos realizados contra esta factura.
     *
     * <p>Lista de todos los intentos de pago (exitosos y fallidos)
     * asociados a esta factura.</p>
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Payment> payments = new HashSet<>();

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public Invoice() {
        this.status = InvoiceStatus.DRAFT;
    }

    /**
     * Constructor principal para crear una nueva factura.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas se manejan en el service layer.</p>
     *
     * @param account la cuenta que será facturada
     * @param booking la reserva que origina la factura
     * @param invoiceNumber número único de identificación
     * @param issueDate fecha de emisión
     * @param dueDate fecha límite de pago
     * @param subtotal subtotal antes de cargos adicionales
     * @param platformFee tarifa de la plataforma
     * @param totalAmount monto total a pagar
     * @param notes notas adicionales (opcional)
     */
    public Invoice(Account account, Booking booking, String invoiceNumber, LocalDateTime issueDate,
                   LocalDateTime dueDate, BigDecimal subtotal, BigDecimal platformFee,
                   BigDecimal totalAmount, String notes) {
        this.account = account;
        this.booking = booking;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.platformFee = platformFee;
        this.totalAmount = totalAmount;
        this.notes = notes;
        this.status = InvoiceStatus.DRAFT;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public Invoice(Long id, Account account, Booking booking, String invoiceNumber, LocalDateTime issueDate,
                   LocalDateTime dueDate, BigDecimal subtotal, BigDecimal platformFee, BigDecimal totalAmount,
                   InvoiceStatus status, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.account = account;
        this.booking = booking;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.platformFee = platformFee;
        this.totalAmount = totalAmount;
        this.status = status;
        this.notes = notes;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Set<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(Set<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public Set<Payment> getPayments() {
        return payments;
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }

    // ========== MÉTODOS DE UTILIDAD PARA RELACIONES BIDIRECCIONALES ==========

    /**
     * Agrega un item a la factura manteniendo la consistencia bidireccional.
     */
    public void addInvoiceItem(InvoiceItem item) {
        invoiceItems.add(item);
        item.setInvoice(this);
    }

    /**
     * Remueve un item de la factura manteniendo la consistencia bidireccional.
     */
    public void removeInvoiceItem(InvoiceItem item) {
        invoiceItems.remove(item);
        item.setInvoice(null);
    }

    /**
     * Agrega un pago a la factura manteniendo la consistencia bidireccional.
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
    }

    /**
     * Remueve un pago de la factura manteniendo la consistencia bidireccional.
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setInvoice(null);
    }

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Verifica si la factura puede ser enviada al cliente.
     *
     * @return true si el estado es DRAFT
     */
    public boolean canBeSent() {
        return InvoiceStatus.DRAFT.equals(this.status);
    }

    /**
     * Verifica si la factura puede recibir pagos.
     *
     * @return true si el estado es SENT, OVERDUE o PARTIALLY_PAID
     */
    public boolean canReceivePayments() {
        return InvoiceStatus.SENT.equals(this.status) ||
                InvoiceStatus.OVERDUE.equals(this.status) ||
                InvoiceStatus.PARTIALLY_PAID.equals(this.status);
    }

    /**
     * Verifica si la factura está completamente pagada.
     *
     * @return true si el estado es PAID
     */
    public boolean isPaid() {
        return InvoiceStatus.PAID.equals(this.status);
    }

    /**
     * Verifica si la factura está vencida.
     *
     * @return true si el estado es OVERDUE o si la fecha actual supera dueDate
     */
    public boolean isOverdue() {
        return InvoiceStatus.OVERDUE.equals(this.status) ||
                (dueDate != null && LocalDateTime.now().isAfter(dueDate) && canReceivePayments());
    }

    /**
     * Verifica si la factura está en un estado final.
     *
     * @return true si el estado es PAID, CANCELLED o REFUNDED
     */
    public boolean isFinalState() {
        return InvoiceStatus.PAID.equals(this.status) ||
                InvoiceStatus.CANCELLED.equals(this.status) ||
                InvoiceStatus.REFUNDED.equals(this.status);
    }

    /**
     * Verifica si la factura puede ser cancelada.
     *
     * @return true si el estado es DRAFT o SENT
     */
    public boolean canBeCancelled() {
        return InvoiceStatus.DRAFT.equals(this.status) ||
                InvoiceStatus.SENT.equals(this.status);
    }

    /**
     * Calcula los días restantes hasta el vencimiento.
     *
     * @return días hasta vencimiento, negativo si ya venció
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el ID único y el número de factura.
     *
     * <p>Se incluye el número de factura como campo de negocio único para garantizar
     * consistencia en colecciones hash incluso durante el ciclo de vida de la entidad.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && invoice.id != null) {
            return Objects.equals(this.id, invoice.id);
        }

        // Si no tienen ID, comparar por número de factura único
        return Objects.equals(getInvoiceNumber(), invoice.getInvoiceNumber());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getInvoiceNumber());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y problemas de rendimiento.</p>
     */
    @Override
    public String toString() {
        return String.format("Invoice{id=%d, invoiceNumber='%s', status=%s, totalAmount=%s, issueDate=%s, createdAt=%s}",
                id, invoiceNumber, status, totalAmount, issueDate, createdAt);
    }
}