package com.Petcare.Petcare.DTOs.Booking;

import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeDTO;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar una vista completa y detallada de una reserva.
 *
 * <p>Este DTO incluye toda la información relevante de una reserva, con datos
 * denormalizados de las entidades relacionadas.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Booking
 * @see BookingSummaryResponse
 */
@Schema(description = "DTO para representar una vista completa y detallada de una reserva.")
public record BookingDetailResponse(
        @Schema(description = "Identificador único de la reserva.", example = "1")
        Long id,

        // Información de la mascota
        @Schema(description = "Identificador de la mascota que recibe el servicio.", example = "1")
        Long petId,

        @Schema(description = "Nombre de la mascota para mostrar en la interfaz.", example = "Fido")
        String petName,

        // Información del cuidador
        @Schema(description = "Identificador del cuidador asignado.", example = "2")
        Long sitterId,

        @Schema(description = "Nombre completo del cuidador responsable del servicio.", example = "Juan Pérez")
        String sitterName,

        // Información del servicio
        @Schema(description = "Identificador de la oferta de servicio contratada.", example = "1")
        Long serviceOfferingId,

        @Schema(description = "Título descriptivo del servicio contratado.", example = "Paseo de 30 minutos")
        String serviceName,

        // Información del cliente
        @Schema(description = "Identificador del usuario que creó la reserva.", example = "3")
        Long bookedByUserId,

        @Schema(description = "Nombre completo del usuario que realizó la reserva.", example = "María García")
        String bookedByUserName,

        // Datos temporales
        @Schema(description = "Fecha y hora programada de inicio del servicio.", example = "2025-02-15T09:00:00")
        LocalDateTime startTime,

        @Schema(description = "Fecha y hora programada de finalización del servicio.", example = "2025-02-15T11:00:00")
        LocalDateTime endTime,

        @Schema(description = "Fecha y hora real de inicio del servicio.", example = "2025-02-15T09:05:00")
        LocalDateTime actualStartTime,

        @Schema(description = "Fecha y hora real de finalización del servicio.", example = "2025-02-15T10:55:00")
        LocalDateTime actualEndTime,

        // Estado y precio
        @Schema(description = "Estado actual de la reserva en el flujo de trabajo.", example = "COMPLETED")
        BookingStatus status,

        @Schema(description = "Precio total acordado para el servicio completo.", example = "50.00")
        BigDecimal totalPrice,

        // Información adicional
        @Schema(description = "Notas adicionales sobre la reserva o instrucciones especiales.", example = "Usar correa larga")
        String notes,

        @Schema(description = "Motivo de cancelación si la reserva fue cancelada.", example = "El cliente canceló")
        String cancellationReason,

        // Metadatos de auditoría
        @Schema(description = "Fecha y hora de creación de la reserva.", example = "2025-02-10T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última modificación.", example = "2025-02-15T11:00:00")
        LocalDateTime updatedAt,

        // Información financiera
        @Schema(description = "Desglose detallado de las tarifas de plataforma aplicadas.")
        PlatformFeeDTO platformFee
) {

    /**
     * Crea una instancia de BookingDetailResponse desde una entidad Booking.
     *
     * @param booking la entidad Booking a convertir
     * @return nueva instancia de BookingDetailResponse con datos poblados
     * @throws IllegalArgumentException si booking es null
     */
    public static BookingDetailResponse fromEntity(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("La entidad Booking no puede ser null");
        }

        return new BookingDetailResponse(
                booking.getId(),
                booking.getPet() != null ? booking.getPet().getId() : null,
                booking.getPet() != null ? booking.getPet().getName() : null,
                booking.getSitter() != null ? booking.getSitter().getId() : null,
                formatUserName(
                        booking.getSitter() != null ? booking.getSitter().getFirstName() : null,
                        booking.getSitter() != null ? booking.getSitter().getLastName() : null
                ),
                booking.getServiceOffering() != null ? booking.getServiceOffering().getId() : null,
                booking.getServiceOffering() != null ? booking.getServiceOffering().getName() : null,
                booking.getBookedByUser() != null ? booking.getBookedByUser().getId() : null,
                formatUserName(
                        booking.getBookedByUser() != null ? booking.getBookedByUser().getFirstName() : null,
                        booking.getBookedByUser() != null ? booking.getBookedByUser().getLastName() : null
                ),
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
                booking.getPlatformFee() != null ? new PlatformFeeDTO(booking.getPlatformFee()) : null
        );
    }

    /**
     * Formatea un nombre completo de usuario de manera segura.
     */
    private static String formatUserName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        return (first + " " + last).trim();
    }

    /**
     * Verifica si la reserva está en un estado activo (confirmada o en progreso).
     */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.IN_PROGRESS;
    }

    /**
     * Verifica si la reserva puede ser cancelada por el cliente.
     */
    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Verifica si el servicio ha sido completado exitosamente.
     */
    public boolean isCompleted() {
        return status == BookingStatus.COMPLETED;
    }

    /**
     * Calcula la duración programada del servicio en horas.
     */
    public long getScheduledDurationInHours() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toHours();
        }
        return 0;
    }

    /**
     * Calcula la duración real del servicio en horas.
     */
    public long getActualDurationInHours() {
        if (actualStartTime != null && actualEndTime != null) {
            return java.time.Duration.between(actualStartTime, actualEndTime).toHours();
        }
        return 0;
    }
}
