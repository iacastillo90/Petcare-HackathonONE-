package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

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
 * @version 1.0
 * @since 1.0
 * @see com.Petcare.Petcare.Controllers.DashboardController
 */
@Data
@AllArgsConstructor
@Schema(description = "DTO principal que consolida toda la información necesaria para el dashboard del cliente.")
public class MainDashboardDTO {

    /**
     * Información del perfil del usuario actualmente autenticado, como su nombre e iniciales.
     * @see UserProfileDTO
     */
    private UserProfileDTO userProfile;

    /**
     * Un resumen de la próxima cita o reserva que el usuario tiene programada.
     * Puede ser nulo si no hay citas futuras.
     * @see BookingSummaryResponse
     */
    private BookingSummaryResponse nextAppointment;

    /**
     * Una lista con el resumen de todas las mascotas asociadas a la cuenta del usuario.
     * @see PetSummaryResponse
     */
    private List<PetSummaryResponse> userPets;

    /**
     * Una lista de cuidadores contactados o reservados recientemente, para facilitar el acceso rápido.
     * @see SitterProfileSummary
     */
    private List<SitterProfileSummary> recentSitters;

    /**
     * Un objeto que contiene las estadísticas y métricas clave de la cuenta del usuario
     * (ej. número de mascotas, citas pendientes, etc.).
     * @see DashboardStatsDTO
     */
    private DashboardStatsDTO stats;
}