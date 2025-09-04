package com.Petcare.Petcare.Models.Payment;

import com.Petcare.Petcare.Models.Invoice.Invoice;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un intento de pago procesado a través de la pasarela de pagos.
 *
 * <p>Esta entidad registra cada transacción de pago, tanto exitosa como fallida,
 * proporcionando un historial completo de actividad financiera y permitiendo
 * el seguimiento detallado de los pagos para auditoría y reconciliación.</p>
 *
 * <p><strong>Relaciones principales:</strong></p>
 * <ul>
 *   <li>Asociado a una factura específica ({@link Invoice})</li>
 *   <li>Utiliza un método de pago registrado ({@link PaymentMethod})</li>
 * </ul>
 *
 * <p><strong>Estados posibles:</strong></p>
 * <ul>
 *   <li>PENDING: Pago iniciado, esperando procesamiento</li>
 *   <li>PROCESSING: En proceso por la pasarela</li>
 *   <li>COMPLETED: Pago exitoso y confirmado</li>
 *   <li>FAILED: Pago rechazado o fallido</li>
 *   <li>CANCELLED: Pago cancelado por el usuario</li>
 *   <li>REFUNDED: Pago reembolsado total o parcialmente</li>
 *   <li>DISPUTED: En disputa o contracargo</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> La comunicación con la pasarela de pagos y
 * las validaciones de negocio se manejan en el service layer.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Invoice
 * @see PaymentMethod
 * @see PaymentStatus
 */
@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_invoice_id", columnList = "invoice_id"),
                @Index(name = "idx_payment_method_id", columnList = "payment_method_id"),
                @Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_processed_at", columnList = "processed_at"),
                @Index(name = "idx_payment_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    /**
     * Identificador único del pago.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Factura a la cual se aplica este pago.
     *
     * <p>Relación obligatoria que establece qué factura está siendo pagada.
     * Un pago puede cubrir parcial o totalmente una factura.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_invoice"))
    @NotNull(message = "La factura es obligatoria para procesar un pago")
    private Invoice invoice;

    /**
     * Método de pago utilizado para esta transacción.
     *
     * <p>Relación obligatoria que especifica qué tarjeta o método de pago
     * se utilizó. Debe estar registrado y verificado en el sistema.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_method"))
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod;

    /**
     * Monto intentado o procesado en este pago.
     *
     * <p>Puede ser el total de la factura o un pago parcial.
     * Se registra en la moneda de la cuenta asociada a la factura.</p>
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El monto debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal amount;

    /**
     * ID de transacción proporcionado por la pasarela de pagos.
     *
     * <p>Identificador único generado por el proveedor de pagos (Stripe, PayPal, etc.)
     * para rastrear la transacción en sus sistemas. Esencial para reconciliación
     * y resolución de disputas.</p>
     */
    @Column(name = "transaction_id", length = 255)
    @Size(max = 255, message = "El ID de transacción no puede exceder 255 caracteres")
    private String transactionId;

    /**
     * Respuesta completa de la pasarela de pagos.
     *
     * <p>Almacena la respuesta JSON o texto completo devuelto por la pasarela
     * para debugging, auditoría y resolución de problemas. Incluye códigos
     * de error, mensajes descriptivos y metadatos adicionales.</p>
     */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    @Size(max = 5000, message = "La respuesta de la pasarela no puede exceder 5000 caracteres")
    private String gatewayResponse;

    /**
     * Estado actual del pago.
     *
     * <p>Refleja el resultado del procesamiento y determina las acciones
     * disponibles. Se actualiza conforme progresa la transacción.</p>
     *
     * @see PaymentStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "El estado del pago es obligatorio")
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Fecha y hora en que se procesó exitosamente el pago.
     *
     * <p>Campo opcional que se establece únicamente cuando el pago
     * se completa exitosamente. Útil para reportes financieros y
     * cálculo de tiempos de procesamiento.</p>
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Código de autorización proporcionado por la pasarela.
     *
     * <p>Código único que confirma la autorización del pago por parte
     * del banco emisor. Se utiliza para referencias y posibles reversiones.</p>
     */
    @Column(name = "authorization_code", length = 50)
    @Size(max = 50, message = "El código de autorización no puede exceder 50 caracteres")
    private String authorizationCode;

    /**
     * Mensaje de error si el pago falló.
     *
     * <p>Descripción legible del motivo de fallo cuando el estado es FAILED.
     * Ayuda en el debugging y proporciona información útil al usuario.</p>
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    @Size(max = 1000, message = "El motivo de fallo no puede exceder 1000 caracteres")
    private String failureReason;

    /**
     * Fecha y hora de creación del registro de pago.
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
     * Útil para auditoría y seguimiento de cambios de estado.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public Payment() {
        this.status = PaymentStatus.PENDING;
    }

    /**
     * Constructor principal para crear un nuevo pago.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas y la comunicación con la pasarela se manejan en el service layer.</p>
     *
     * @param invoice la factura que se está pagando
     * @param paymentMethod el método de pago utilizado
     * @param amount el monto a procesar
     */
    public Payment(Invoice invoice, PaymentMethod paymentMethod, BigDecimal amount) {
        this.invoice = invoice;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    /**
     * Constructor completo para crear un pago con información de procesamiento.
     *
     * @param invoice la factura que se está pagando
     * @param paymentMethod el método de pago utilizado
     * @param amount el monto procesado
     * @param transactionId ID de la transacción de la pasarela
     * @param gatewayResponse respuesta de la pasarela
     * @param authorizationCode código de autorización
     */
    public Payment(Invoice invoice, PaymentMethod paymentMethod, BigDecimal amount,
                   String transactionId, String gatewayResponse, String authorizationCode) {
        this.invoice = invoice;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
        this.authorizationCode = authorizationCode;
        this.status = PaymentStatus.PENDING;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public Payment(Long id, Invoice invoice, PaymentMethod paymentMethod, BigDecimal amount,
                   String transactionId, String gatewayResponse, PaymentStatus status,
                   LocalDateTime processedAt, String authorizationCode, String failureReason,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoice = invoice;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
        this.status = status;
        this.processedAt = processedAt;
        this.authorizationCode = authorizationCode;
        this.failureReason = failureReason;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si el pago fue procesado exitosamente.
     *
     * @return true si el estado es COMPLETED
     */
    public boolean isSuccessful() {
        return PaymentStatus.COMPLETED.equals(this.status);
    }

    /**
     * Verifica si el pago está en un estado final (no puede cambiar).
     *
     * @return true si el estado es COMPLETED, FAILED, CANCELLED o REFUNDED
     */
    public boolean isFinalState() {
        return PaymentStatus.COMPLETED.equals(this.status) ||
                PaymentStatus.FAILED.equals(this.status) ||
                PaymentStatus.CANCELLED.equals(this.status) ||
                PaymentStatus.REFUNDED.equals(this.status);
    }

    /**
     * Verifica si el pago está pendiente de procesamiento.
     *
     * @return true si el estado es PENDING o PROCESSING
     */
    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status) ||
                PaymentStatus.PROCESSING.equals(this.status);
    }

    /**
     * Marca el pago como completado exitosamente.
     *
     * <p>Método de conveniencia que actualiza el estado y establece
     * la fecha de procesamiento. Debe ser llamado desde el service layer.</p>
     *
     * @param authCode código de autorización de la pasarela
     */
    public void markAsCompleted(String authCode) {
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
        this.authorizationCode = authCode;
        this.failureReason = null;
    }

    /**
     * Marca el pago como fallido.
     *
     * <p>Método de conveniencia que actualiza el estado y registra
     * el motivo del fallo.</p>
     *
     * @param reason motivo del fallo proporcionado por la pasarela
     */
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el ID único y campos de negocio clave.
     *
     * <p>Se incluyen campos inmutables para garantizar consistencia en colecciones
     * hash incluso durante el ciclo de vida de la entidad.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && payment.id != null) {
            return Objects.equals(this.id, payment.id);
        }

        // Si no tienen ID, comparar por transactionId (si existe) o campos de negocio
        if (this.transactionId != null && payment.transactionId != null) {
            return Objects.equals(this.transactionId, payment.transactionId);
        }

        return Objects.equals(getInvoice(), payment.getInvoice()) &&
                Objects.equals(getPaymentMethod(), payment.getPaymentMethod()) &&
                Objects.equals(getAmount(), payment.getAmount()) &&
                Objects.equals(getCreatedAt(), payment.getCreatedAt());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        if (transactionId != null) {
            return Objects.hash(transactionId);
        }
        return Objects.hash(getInvoice(), getPaymentMethod(), getAmount(), getCreatedAt());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y problemas de rendimiento. No incluye datos sensibles.</p>
     */
    @Override
    public String toString() {
        return String.format("Payment{id=%d, status=%s, amount=%s, transactionId='%s', processedAt=%s, createdAt=%s}",
                id, status, amount, transactionId, processedAt, createdAt);
    }
}