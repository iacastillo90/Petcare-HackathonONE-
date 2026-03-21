package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * DTO principal que consolida toda la información necesaria para el dashboard del cliente.
 * <p>
 * Este objeto actúa como un agregador, reuniendo diferentes piezas de información de varios
 * servicios en una única respuesta cohesiva. Su propósito es optimizar la carga del panel
 * de control principal del usuario, permitiendo que el frontend construya toda la vista
 * con una sola llamada a la API.
 * </p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see com.Petcare.Petcare.Controllers.DashboardController
 */
@Schema(description = "DTO principal que consolida toda la información necesaria para el dashboard del cliente.")
public record MainDashboardDTO(
        @Schema(description = "Información del perfil del usuario actualmente autenticado.")
        UserProfileDTO userProfile,

        @Schema(description = "Resumen de la próxima cita o reserva programada. Puede ser nulo si no hay citas futuras.")
        BookingSummaryResponse nextAppointment,

        @Schema(description = "Lista con el resumen de todas las mascotas asociadas a la cuenta del usuario.")
        List<PetSummaryResponse> userPets,

        @Schema(description = "Lista de cuidadores contactados o reservados recientemente.")
        List<SitterProfileSummary> recentSitters,

        @Schema(description = "Estadísticas y métricas clave de la cuenta del usuario.")
        DashboardStatsDTO stats
) { }
