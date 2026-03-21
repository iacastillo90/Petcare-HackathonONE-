package com.Petcare.Petcare.DTOs.Booking;

import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar una vista resumida y optimizada de una reserva.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Booking
 * @see BookingDetailResponse
 */
@Schema(description = "DTO para representar una vista resumida y optimizada de una reserva.")
public record BookingSummaryResponse(
        @Schema(description = "Identificador único de la reserva.", example = "1")
        Long id,

        @Schema(description = "Nombre de la mascota que recibe el servicio.", example = "Fido")
        String petName,

        @Schema(description = "Nombre completo del cuidador asignado.", example = "Juan Pérez")
        String sitterName,

        @Schema(description = "Fecha y hora programada de inicio del servicio.", example = "2025-02-15T09:00:00")
        LocalDateTime startTime,

        @Schema(description = "Estado actual de la reserva.", example = "CONFIRMED")
        BookingStatus status,

        @Schema(description = "Precio total del servicio.", example = "150.00")
        BigDecimal totalPrice
) {

    /**
     * Crea una instancia de BookingSummaryResponse desde una entidad Booking.
     *
     * @param booking la entidad Booking a convertir
     * @return nueva instancia de BookingSummaryResponse
     * @throws IllegalArgumentException si booking es null
     */
    public static BookingSummaryResponse fromEntity(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("La entidad Booking no puede ser null");
        }
        return new BookingSummaryResponse(
                booking.getId(),
                booking.getPet() != null ? booking.getPet().getName() : null,
                formatUserName(
                        booking.getSitter() != null ? booking.getSitter().getFirstName() : null,
                        booking.getSitter() != null ? booking.getSitter().getLastName() : null
                ),
                booking.getStartTime(),
                booking.getStatus(),
                booking.getTotalPrice()
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
     * Verifica si la reserva requiere atención inmediata.
     */
    public boolean requiresAttention() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Verifica si la reserva está en un estado final.
     */
    public boolean isFinalized() {
        return status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED;
    }

    /**
     * Proporciona una etiqueta de estado localizada para la UI.
     */
    public String statusLabel() {
        if (status == null) return "Desconocido";
        return switch (status) {
            case PENDING -> "Pendiente";
            case CONFIRMED -> "Confirmada";
            case IN_PROGRESS -> "En Progreso";
            case COMPLETED -> "Completada";
            case CANCELLED -> "Cancelada";
        };
    }
}
