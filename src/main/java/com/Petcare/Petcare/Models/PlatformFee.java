package com.Petcare.Petcare.Models;

import com.Petcare.Petcare.Models.Booking.Booking;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que desglosa la tarifa de la plataforma para una reserva específica.
 *
 * <p>Esta entidad es fundamental para la auditoría financiera, ya que registra
 * de forma inmutable cómo se calculó la comisión de Petcare para un servicio
 * completado.</p>
 *
 * <p><strong>Relación principal:</strong></p>
 * <ul>
 * <li>Asociada a una única reserva ({@link Booking})</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Booking
 */
@Entity
@Table(name = "platform_fees",
        indexes = {
                @Index(name = "idx_platformfee_booking_id", columnList = "booking_id", unique = true)
        })
@EntityListeners(AuditingEntityListener.class)
public class PlatformFee {

    /**
     * Identificador único de la tarifa de plataforma.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reserva a la cual esta tarifa está asociada.
     * <p>Esta es la relación propietaria. La tarifa no puede existir sin una reserva.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_platformfee_booking"))
    @NotNull(message = "La reserva es obligatoria para calcular la tarifa")
    private Booking booking;

    /**
     * Monto base sobre el cual se calcula la comisión (generalmente el totalPrice de la reserva).
     */
    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El monto base no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El monto base debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto base debe tener un máximo de 10 dígitos y 2 decimales")
    private BigDecimal baseAmount;

    /**
     * Porcentaje de la comisión aplicado en el momento de la transacción.
     */
    @Column(name = "fee_percentage", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "El porcentaje de la tarifa no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El porcentaje debe ser positivo")
    @Digits(integer = 5, fraction = 2, message = "El porcentaje debe tener un máximo de 5 dígitos y 2 decimales")
    private BigDecimal feePercentage;

    /**
     * Monto exacto de la comisión cobrada por la plataforma (baseAmount * feePercentage).
     */
    @Column(name = "fee_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El monto de la tarifa no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El monto de la tarifa debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto de la tarifa debe tener un máximo de 10 dígitos y 2 decimales")
    private BigDecimal feeAmount;

    /**
     * Monto neto que corresponde al cuidador después de deducir la comisión (baseAmount - feeAmount).
     */
    @Column(name = "net_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El monto neto no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El monto neto debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto neto debe tener un máximo de 10 dígitos y 2 decimales")
    private BigDecimal netAmount;

    /**
     * Fecha y hora de creación del registro de la tarifa.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public PlatformFee() {
    }

    /**
     * Constructor completo para facilitar la creación y pruebas.
     */
    public PlatformFee(Booking booking, BigDecimal baseAmount, BigDecimal feePercentage, BigDecimal feeAmount, BigDecimal netAmount) {
        this.booking = booking;
        this.baseAmount = baseAmount;
        this.feePercentage = feePercentage;
        this.feeAmount = feeAmount;
        this.netAmount = netAmount;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public PlatformFee(Long id, Booking booking, BigDecimal baseAmount, BigDecimal feePercentage, BigDecimal feeAmount, BigDecimal netAmount, LocalDateTime createdAt) {
        this.id = id;
        this.booking = booking;
        this.baseAmount = baseAmount;
        this.feePercentage = feePercentage;
        this.feeAmount = feeAmount;
        this.netAmount = netAmount;
        this.createdAt = createdAt;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     *
     * Implementación de equals basada en el ID único y campos de negocio clave.
     *
     * <p>Se incluyen campos inmutables para garantizar consistencia en colecciones
     * hash incluso durante el ciclo de vida de la entidad.</p>
     *
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformFee that = (PlatformFee) o;
        // Una entidad es igual a otra si su ID no es nulo y es el mismo.
        return id != null && Objects.equals(id, that.id);
    }

    /**
     *
     * Implementación de hashCode que considera el ID único y campos de negocio clave.
     *
     * <p>Se incluyen campos inmutables para garantizar consistencia en colecciones
     * hash incluso durante el ciclo de vida de la entidad.</p>
     */
    @Override
    public int hashCode() {
        // Usa una constante para entidades sin ID (transitorias).
        // Usa el hashCode de la clase para diferenciar de otras entidades con ID nulo.
        return id != null ? id.hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PlatformFee{" +
                "id=" + id +
                ", bookingId=" + (booking != null ? booking.getId() : "null") +
                ", feeAmount=" + feeAmount +
                ", createdAt=" + createdAt +
                '}';
    }
}