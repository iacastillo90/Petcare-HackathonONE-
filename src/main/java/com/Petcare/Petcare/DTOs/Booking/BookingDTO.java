package com.Petcare.Petcare.DTOs.Booking;

import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeDTO;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferir información de reservas de servicios de cuidado de mascotas.
 *
 * <p>Este DTO expone los datos necesarios de {@link Booking} sin exponer
 * la estructura interna de las entidades JPA ni causar problemas de lazy loading.</p>
 *
 * <p>Las relaciones con otras entidades se representan mediante sus IDs
 * y información básica necesaria para la presentación.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 */
public class BookingDTO {

    private Long id;

    // ========== IDS DE RELACIONES ==========

    /**
     * ID de la mascota que recibirá el servicio.
     */
    @NotNull(message = "El ID de la mascota es obligatorio")
    private Long petId;

    /**
     * Nombre de la mascota para mostrar en la UI.
     */
    private String petName;

    /**
     * ID del cuidador asignado.
     */
    @NotNull(message = "El ID del cuidador es obligatorio")
    private Long sitterId;

    /**
     * Nombre completo del cuidador.
     */
    private String sitterName;

    /**
     * ID de la oferta de servicio reservada.
     */
    @NotNull(message = "El ID de la oferta de servicio es obligatorio")
    private Long serviceOfferingId;

    /**
     * Nombre del servicio para mostrar en la UI.
     */
    private String serviceName;

    /**
     * ID del usuario que creó la reserva.
     */
    @NotNull(message = "El ID del usuario que crea la reserva es obligatorio")
    private Long bookedByUserId;

    /**
     * Nombre del usuario que creó la reserva.
     */
    private String bookedByUserName;

    // ========== DATOS PRINCIPALES DE LA RESERVA ==========

    /**
     * Fecha y hora programada de inicio del servicio.
     */
    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime startTime;

    /**
     * Fecha y hora programada de finalización del servicio.
     */
    @NotNull(message = "La fecha y hora de finalización son obligatorias")
    private LocalDateTime endTime;

    /**
     * Fecha y hora real de inicio del servicio.
     */
    private LocalDateTime actualStartTime;

    /**
     * Fecha y hora real de finalización del servicio.
     */
    private LocalDateTime actualEndTime;

    /**
     * Estado actual de la reserva.
     */
    @NotNull(message = "El estado de la reserva es obligatorio")
    private BookingStatus status;

    /**
     * Precio total acordado para el servicio.
     */
    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal totalPrice;

    /**
     * Notas adicionales sobre la reserva.
     */
    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    /**
     * Motivo de cancelación si aplica.
     */
    @Size(max = 1000, message = "El motivo de cancelación no puede exceder 1000 caracteres")
    private String cancellationReason;

    /**
     * Fecha y hora de creación de la reserva.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     */
    private LocalDateTime updatedAt;

    /**
     * DTO de la tarifa de plataforma asociada.
     */
    private PlatformFeeDTO platformFee;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public BookingDTO() {
    }

    /**
     * Constructor para crear DTO desde entidad Booking.
     */
    public BookingDTO(Booking booking) {
        this.id = booking.getId();
        this.petId = booking.getPet() != null ? booking.getPet().getId() : null;
        this.petName = booking.getPet() != null ? booking.getPet().getName() : null;
        this.sitterId = booking.getSitter() != null ? booking.getSitter().getId() : null;
        this.sitterName = booking.getSitter() != null ?
                String.format("%s %s",
                        booking.getSitter().getFirstName(),
                        booking.getSitter().getLastName()).trim() : null;
        this.serviceOfferingId = booking.getServiceOffering() != null ?
                booking.getServiceOffering().getId() : null;
        this.serviceName = booking.getServiceOffering() != null ?
                booking.getServiceOffering().getName() : null;
        this.bookedByUserId = booking.getBookedByUser() != null ?
                booking.getBookedByUser().getId() : null;
        this.bookedByUserName = booking.getBookedByUser() != null ?
                String.format("%s %s",
                        booking.getBookedByUser().getFirstName(),
                        booking.getBookedByUser().getLastName()).trim() : null;
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
        this.actualStartTime = booking.getActualStartTime();
        this.actualEndTime = booking.getActualEndTime();
        this.status = booking.getStatus();
        this.totalPrice = booking.getTotalPrice();
        this.notes = booking.getNotes();
        this.cancellationReason = booking.getCancellationReason();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();
        this.platformFee = booking.getPlatformFee() != null ?
                new PlatformFeeDTO(booking.getPlatformFee()) : null;
    }

    /**
     * Constructor completo para testing y casos específicos.
     */
    public BookingDTO(Long id, Long petId, String petName, Long sitterId, String sitterName,
                      Long serviceOfferingId, String serviceName, Long bookedByUserId,
                      String bookedByUserName, LocalDateTime startTime, LocalDateTime endTime,
                      LocalDateTime actualStartTime, LocalDateTime actualEndTime,
                      BookingStatus status, BigDecimal totalPrice, String notes,
                      String cancellationReason, LocalDateTime createdAt, LocalDateTime updatedAt,
                      PlatformFeeDTO platformFee) {
        this.id = id;
        this.petId = petId;
        this.petName = petName;
        this.sitterId = sitterId;
        this.sitterName = sitterName;
        this.serviceOfferingId = serviceOfferingId;
        this.serviceName = serviceName;
        this.bookedByUserId = bookedByUserId;
        this.bookedByUserName = bookedByUserName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.actualStartTime = actualStartTime;
        this.actualEndTime = actualEndTime;
        this.status = status;
        this.totalPrice = totalPrice;
        this.notes = notes;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.platformFee = platformFee;
    }

    // ========== GETTERS ==========

    public Long getId() {
        return id;
    }

    public Long getPetId() {
        return petId;
    }

    public String getPetName() {
        return petName;
    }

    public Long getSitterId() {
        return sitterId;
    }

    public String getSitterName() {
        return sitterName;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Long getBookedByUserId() {
        return bookedByUserId;
    }

    public String getBookedByUserName() {
        return bookedByUserName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PlatformFeeDTO getPlatformFee() {
        return platformFee;
    }


    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si la reserva está en un estado activo.
     */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.IN_PROGRESS;
    }

    /**
     * Verifica si la reserva puede ser cancelada.
     */
    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Verifica si la reserva está completada.
     */
    public boolean isCompleted() {
        return status == BookingStatus.COMPLETED;
    }

    /**
     * Calcula la duración programada en horas.
     */
    public long getScheduledDurationInHours() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toHours();
        }
        return 0;
    }

    /**
     * Calcula la duración real en horas si está disponible.
     */
    public long getActualDurationInHours() {
        if (actualStartTime != null && actualEndTime != null) {
            return java.time.Duration.between(actualStartTime, actualEndTime).toHours();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "BookingDTO{" +
                "id=" + id +
                ", petName='" + petName + '\'' +
                ", sitterName='" + sitterName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                '}';
    }
}