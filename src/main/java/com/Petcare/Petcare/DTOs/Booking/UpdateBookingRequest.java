package com.Petcare.Petcare.DTOs.Booking;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * DTO para la actualización de reservas existentes.
 *
 * <p>Este DTO contiene únicamente los campos que pueden ser modificados
 * en una reserva existente, aplicando validaciones específicas según
 * el contexto de la actualización.</p>
 *
 * <p><strong>Campos modificables por estado:</strong></p>
 * <ul>
 *   <li>PENDING: Todos los campos disponibles</li>
 *   <li>CONFIRMED: Solo notas y campos administrativos</li>
 *   <li>IN_PROGRESS: Solo tiempos reales y notas</li>
 *   <li>COMPLETED/CANCELLED: Solo por administradores</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 2.0
 */
public class UpdateBookingRequest {

    /**
     * Nueva fecha y hora de inicio del servicio.
     * Solo modificable en estado PENDING.
     */
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime startTime;

    /**
     * Notas actualizadas sobre la reserva.
     * Modificable en todos los estados.
     */
    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    /**
     * Precio total actualizado.
     * Solo modificable por administradores o en casos especiales.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal totalPrice;

    /**
     * Tiempo real de inicio del servicio.
     * Solo modificable por el cuidador cuando el servicio comienza.
     */
    private LocalDateTime actualStartTime;

    /**
     * Tiempo real de finalización del servicio.
     * Solo modificable por el cuidador cuando el servicio termina.
     */
    private LocalDateTime actualEndTime;

    // ========== CONSTRUCTORES ==========

    public UpdateBookingRequest() {}

    public UpdateBookingRequest(LocalDateTime startTime, String notes,
                                BigDecimal totalPrice, LocalDateTime actualStartTime,
                                LocalDateTime actualEndTime) {
        this.startTime = startTime;
        this.notes = notes;
        this.totalPrice = totalPrice;
        this.actualStartTime = actualStartTime;
        this.actualEndTime = actualEndTime;
    }

    // ========== GETTERS Y SETTERS ==========

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }

    @Override
    public String toString() {
        return "UpdateBookingRequest{" +
                "startTime=" + startTime +
                ", notes='" + notes + '\'' +
                ", totalPrice=" + totalPrice +
                ", actualStartTime=" + actualStartTime +
                ", actualEndTime=" + actualEndTime +
                '}';
    }
}