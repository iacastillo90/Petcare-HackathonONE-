package com.Petcare.Petcare.Models.Payment;

import com.Petcare.Petcare.Models.Account.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un método de pago registrado para una cuenta.
 *
 * <p>Esta entidad almacena de forma segura la información de tarjetas de crédito,
 * débito y otros métodos de pago asociados a una cuenta. Se integra con pasarelas
 * de pago externas para procesar transacciones sin almacenar datos sensibles
 * directamente en el sistema.</p>
 *
 * <p><strong>Relaciones principales:</strong></p>
 * <ul>
 *   <li>Pertenece a una cuenta específica ({@link Account})</li>
 *   <li>Puede ser utilizado en múltiples pagos ({@link Payment})</li>
 * </ul>
 *
 * <p><strong>Tipos de método de pago soportados:</strong></p>
 * <ul>
 *   <li>CREDIT_CARD: Tarjetas de crédito (Visa, MasterCard, etc.)</li>
 *   <li>DEBIT_CARD: Tarjetas de débito</li>
 *   <li>BANK_ACCOUNT: Transferencias bancarias directas</li>
 *   <li>DIGITAL_WALLET: Billeteras digitales (PayPal, etc.)</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>No se almacenan números completos de tarjeta</li>
 *   <li>Se utiliza tokenización via gatewayToken</li>
 *   <li>Solo se guardan los últimos 4 dígitos para identificación</li>
 *   <li>Verificación obligatoria antes del primer uso</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> La comunicación con pasarelas de pago y
 * validaciones de seguridad se manejan en el service layer.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Account
 * @see Payment
 */
@Entity
@Table(name = "payment_methods",
        indexes = {
                @Index(name = "idx_paymentmethod_account_id", columnList = "account_id"),
                @Index(name = "idx_paymentmethod_gateway_token", columnList = "gateway_token"),
                @Index(name = "idx_paymentmethod_default", columnList = "is_default"),
                @Index(name = "idx_paymentmethod_active", columnList = "is_active"),
                @Index(name = "idx_paymentmethod_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class PaymentMethod {

    /**
     * Identificador único del método de pago.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cuenta a la cual pertenece este método de pago.
     *
     * <p>Relación obligatoria que establece qué cuenta puede utilizar
     * este método para procesar pagos.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentmethod_account"))
    @NotNull(message = "La cuenta es obligatoria para registrar un método de pago")
    private Account account;

    /**
     * Tipo de tarjeta o método de pago.
     *
     * <p>Identifica el proveedor o tipo específico del método de pago.
     * Ejemplos: "VISA", "MASTERCARD", "AMEX", "BANK_TRANSFER".</p>
     */
    @Column(name = "card_type", nullable = false, length = 50)
    @NotBlank(message = "El tipo de tarjeta es obligatorio")
    @Size(max = 50, message = "El tipo de tarjeta no puede exceder 50 caracteres")
    private String cardType;

    /**
     * Últimos cuatro dígitos de la tarjeta o identificador parcial.
     *
     * <p>Para tarjetas: últimos 4 dígitos (ej: "4242").
     * Para cuentas bancarias: últimos dígitos de la cuenta.
     * Se muestra al usuario para identificar el método sin exponer datos sensibles.</p>
     */
    @Column(name = "last_four_digits", nullable = false, length = 4)
    @NotBlank(message = "Los últimos 4 dígitos son obligatorios")
    @Size(min = 4, max = 4, message = "Deben ser exactamente 4 dígitos")
    @Pattern(regexp = "^\\d{4}$", message = "Los últimos dígitos deben ser 4 números")
    private String lastFourDigits;

    /**
     * Fecha de expiración del método de pago.
     *
     * <p>Para tarjetas: mes/año de expiración (ej: "12/25").
     * Para métodos sin expiración, puede ser null.</p>
     */
    @Column(name = "expiry_date", length = 7)
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "La fecha de expiración debe tener formato MM/YY")
    private String expiryDate;

    /**
     * Token seguro proporcionado por la pasarela de pagos.
     *
     * <p>Identificador único y seguro generado por el proveedor de pagos
     * (Stripe, PayPal, etc.) que permite procesar transacciones sin
     * almacenar datos sensibles en nuestro sistema.</p>
     */
    @Column(name = "gateway_token", nullable = false, length = 255)
    @NotBlank(message = "El token de la pasarela es obligatorio")
    @Size(max = 255, message = "El token no puede exceder 255 caracteres")
    private String gatewayToken;

    /**
     * Indica si este es el método de pago por defecto de la cuenta.
     *
     * <p>Solo un método de pago por cuenta puede ser el predeterminado.
     * Se utiliza automáticamente cuando no se especifica otro método.</p>
     */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    /**
     * Indica si el método de pago ha sido verificado.
     *
     * <p>La verificación se realiza mediante micro-transacciones o
     * otros métodos proporcionados por la pasarela. Un método no
     * verificado no puede ser utilizado para pagos.</p>
     */
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    /**
     * Indica si el método de pago está activo y disponible para uso.
     *
     * <p>Los métodos inactivos se mantienen para referencia histórica
     * pero no pueden ser utilizados para nuevas transacciones.</p>
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Fecha y hora de creación del método de pago.
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
     * Útil para auditoría y seguimiento de cambios.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Pagos realizados utilizando este método.
     *
     * <p>Historial de todas las transacciones procesadas con este método,
     * tanto exitosas como fallidas.</p>
     */
    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Payment> payments = new HashSet<>();

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public PaymentMethod() {
    }

    /**
     * Constructor principal para registrar un nuevo método de pago.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas y la comunicación con la pasarela se manejan en el service layer.</p>
     *
     * @param account la cuenta propietaria
     * @param cardType tipo de tarjeta o método
     * @param lastFourDigits últimos 4 dígitos
     * @param expiryDate fecha de expiración (puede ser null)
     * @param gatewayToken token seguro de la pasarela
     */
    public PaymentMethod(Account account, String cardType, String lastFourDigits,
                         String expiryDate, String gatewayToken) {
        this.account = account;
        this.cardType = cardType;
        this.lastFourDigits = lastFourDigits;
        this.expiryDate = expiryDate;
        this.gatewayToken = gatewayToken;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public PaymentMethod(Long id, Account account, String cardType, String lastFourDigits,
                         String expiryDate, String gatewayToken, boolean isDefault,
                         boolean isVerified, boolean isActive, LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.account = account;
        this.cardType = cardType;
        this.lastFourDigits = lastFourDigits;
        this.expiryDate = expiryDate;
        this.gatewayToken = gatewayToken;
        this.isDefault = isDefault;
        this.isVerified = isVerified;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getGatewayToken() {
        return gatewayToken;
    }

    public void setGatewayToken(String gatewayToken) {
        this.gatewayToken = gatewayToken;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
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

    public Set<Payment> getPayments() {
        return payments;
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }

    // ========== MÉTODOS DE UTILIDAD PARA RELACIONES BIDIRECCIONALES ==========

    /**
     * Agrega un pago a este método manteniendo la consistencia bidireccional.
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setPaymentMethod(this);
    }

    /**
     * Remueve un pago de este método manteniendo la consistencia bidireccional.
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setPaymentMethod(null);
    }

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Verifica si el método de pago puede ser utilizado para transacciones.
     *
     * @return true si está activo, verificado y no expirado
     */
    public boolean isUsable() {
        return this.isActive && this.isVerified && !isExpired();
    }

    /**
     * Verifica si el método de pago ha expirado.
     *
     * @return true si la fecha de expiración ha pasado
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false; // Métodos sin expiración nunca expiran
        }

        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]); // Convertir YY a YYYY

            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            return year < currentYear || (year == currentYear && month < currentMonth);
        } catch (Exception e) {
            return true; // Si no se puede parsear, considerar expirado
        }
    }

    /**
     * Obtiene una representación amigable del método de pago.
     *
     * @return descripción legible (ej: "Visa terminada en 4242")
     */
    public String getDisplayName() {
        if (lastFourDigits != null && cardType != null) {
            return String.format("%s terminada en %s", cardType, lastFourDigits);
        }
        return cardType != null ? cardType : "Método de pago";
    }

    /**
     * Marca este método como el predeterminado para la cuenta.
     *
     * <p>Nota: El service layer debe asegurarse de que solo un método
     * sea el predeterminado por cuenta.</p>
     */
    public void markAsDefault() {
        this.isDefault = true;
    }

    /**
     * Remueve la marca de predeterminado de este método.
     */
    public void unmarkAsDefault() {
        this.isDefault = false;
    }

    /**
     * Marca el método como verificado después de la validación exitosa.
     */
    public void markAsVerified() {
        this.isVerified = true;
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
        if (!(o instanceof PaymentMethod)) return false;
        PaymentMethod that = (PaymentMethod) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // Si no tienen ID, comparar por gatewayToken (único en la pasarela)
        return Objects.equals(getGatewayToken(), that.getGatewayToken());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getGatewayToken());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y exposición de datos sensibles.</p>
     */
    @Override
    public String toString() {
        return String.format("PaymentMethod{id=%d, cardType='%s', lastFourDigits='%s', isDefault=%s, isVerified=%s, isActive=%s, createdAt=%s}",
                id, cardType, lastFourDigits, isDefault, isVerified, isActive, createdAt);
    }
}