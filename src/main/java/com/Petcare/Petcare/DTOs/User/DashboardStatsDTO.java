package com.Petcare.Petcare.DTOs.User;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para encapsular las estadísticas clave del dashboard de un cliente.
 * <p>
 * Este objeto está diseñado para proporcionar una vista rápida y resumida del estado
 * de la cuenta de un usuario, ideal para poblar tarjetas de métricas o widgets en
 * el panel principal del cliente.
 * </p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see com.Petcare.Petcare.Controllers.DashboardController
 * @see MainDashboardDTO
 */
@Schema(description = "DTO para encapsular las estadísticas clave del dashboard de un cliente.")
public record DashboardStatsDTO(
        @Schema(description = "Número total de mascotas activas asociadas a la cuenta del usuario.", example = "3")
        int activePets,

        @Schema(description = "Texto descriptivo contextual para la métrica de mascotas activas.", example = "Total de mascotas activas")
        String activePetsChange,

        @Schema(description = "Número de citas o reservas futuras programadas.", example = "2")
        int scheduledAppointments,

        @Schema(description = "Texto descriptivo contextual para la métrica de citas programadas.", example = "Próximas citas")
        String scheduledAppointmentsChange,

        @Schema(description = "Estado de las vacunas de las mascotas en formato actualizado/total.", example = "3/4")
        String vaccinesUpToDate,

        @Schema(description = "Texto descriptivo contextual para la métrica de vacunas.", example = "1 pendiente")
        String vaccinesChange,

        @Schema(description = "Número de recordatorios o eventos importantes pendientes.", example = "1")
        int pendingReminders,

        @Schema(description = "Texto descriptivo contextual para la métrica de recordatorios pendientes.", example = "Evento próximo")
        String pendingRemindersChange
) { }
