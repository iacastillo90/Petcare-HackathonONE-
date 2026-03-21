package com.Petcare.Petcare.DTOs.Booking;

import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeDTO;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferir información de reservas de servicios de cuidado de mascotas.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Booking
 */
@Schema(description = "DTO para transferir información de reservas de servicios de cuidado de mascotas.")
public record BookingDTO(
        @Schema(description = "Identificador único de la reserva.", example = "1")
        Long id,

        // IDs de relaciones
        @NotNull(message = "El ID de la mascota es obligatorio")
        @Schema(description = "ID de la mascota que recibirá el servicio.", example = "1")
        Long petId,

        @Schema(description = "Nombre de la mascota para mostrar en la UI.", example = "Fido")
        String petName,

        @NotNull(message = "El ID del cuidador es obligatorio")
        @Schema(description = "ID del cuidador asignado.", example = "2")
        Long sitterId,

        @Schema(description = "Nombre completo del cuidador.", example = "Juan Pérez")
        String sitterName,

        @NotNull(message = "El ID de la oferta de servicio es obligatorio")
        @Schema(description = "ID de la oferta de servicio reservada.", example = "1")
        Long serviceOfferingId,

        @Schema(description = "Nombre del servicio para mostrar en la UI.", example = "Paseo de 30 minutos")
        String serviceName,

        @NotNull(message = "El ID del usuario que crea la reserva es obligatorio")
        @Schema(description = "ID del usuario que creó la reserva.", example = "3")
        Long bookedByUserId,

        @Schema(description = "Nombre del usuario que creó la reserva.", example = "María García")
        String bookedByUserName,

        // Datos principales de la reserva
        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        @Future(message = "La fecha de inicio debe ser futura")
        @Schema(description = "Fecha y hora programada de inicio del servicio.", example = "2025-02-15T09:00:00")
        LocalDateTime startTime,

        @NotNull(message = "La fecha y hora de finalización son obligatorias")
        @Schema(description = "Fecha y hora programada de finalización del servicio.", example = "2025-02-15T11:00:00")
        LocalDateTime endTime,

        @Schema(description = "Fecha y hora real de inicio del servicio.", example = "2025-02-15T09:05:00")
        LocalDateTime actualStartTime,

        @Schema(description = "Fecha y hora real de finalización del servicio.", example = "2025-02-15T10:55:00")
        LocalDateTime actualEndTime,

        @NotNull(message = "El estado de la reserva es obligatorio")
        @Schema(description = "Estado actual de la reserva.", example = "PENDING")
        BookingStatus status,

        @NotNull(message = "El precio total es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
        @Schema(description = "Precio total acordado para el servicio.", example = "50.00")
        BigDecimal totalPrice,

        @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
        @Schema(description = "Notas adicionales sobre la reserva.", example = "Usar correa larga")
        String notes,

        @Size(max = 1000, message = "El motivo de cancelación no puede exceder 1000 caracteres")
        @Schema(description = "Motivo de cancelación si aplica.", example = "Cambio de planes")
        String cancellationReason,

        @Schema(description = "Fecha y hora de creación de la reserva.", example = "2025-02-10T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización.", example = "2025-02-10T14:30:00")
        LocalDateTime updatedAt,

        @Schema(description = "DTO de la tarifa de plataforma asociada.")
        PlatformFeeDTO platformFee
) {

    /**
     * Constructor para crear DTO desde entidad Booking.
     */
    public BookingDTO(Booking booking) {
        this(
                booking.getId(),
                booking.getPet() != null ? booking.getPet().getId() : null,
                booking.getPet() != null ? booking.getPet().getName() : null,
                booking.getSitter() != null ? booking.getSitter().getId() : null,
                booking.getSitter() != null ?
                        String.format("%s %s",
                                booking.getSitter().getFirstName(),
                                booking.getSitter().getLastName()).trim() : null,
                booking.getServiceOffering() != null ?
                        booking.getServiceOffering().getId() : null,
                booking.getServiceOffering() != null ?
                        booking.getServiceOffering().getName() : null,
                booking.getBookedByUser() != null ?
                        booking.getBookedByUser().getId() : null,
                booking.getBookedByUser() != null ?
                        String.format("%s %s",
                                booking.getBookedByUser().getFirstName(),
                                booking.getBookedByUser().getLastName()).trim() : null,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getActualStartTime(),
                booking.getActualEndTime(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getNotes(),
                booking.getCancellationReason(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                booking.getPlatformFee() != null ?
                        new PlatformFeeDTO(booking.getPlatformFee()) : null
        );
    }

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
}
