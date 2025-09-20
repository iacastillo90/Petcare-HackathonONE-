package com.Petcare.Petcare.DTOs.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para encapsular las estadísticas clave del dashboard de un cliente.
 * <p>
 * Este objeto está diseñado para proporcionar una vista rápida y resumida del estado
 * de la cuenta de un usuario, ideal para poblar tarjetas de métricas o widgets en
 * el panel principal del cliente.
 * </p>
 * <p>Cada métrica principal se compone de un valor numérico o de estado y un texto
 * descriptivo complementario para la interfaz de usuario.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see com.Petcare.Petcare.Controllers.DashboardController
 * @see com.Petcare.Petcare.DTOs.User.MainDashboardDTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para encapsular las estadísticas clave del dashboard de un cliente.")
public class DashboardStatsDTO {

    /**
     * Número total de mascotas activas asociadas a la cuenta del usuario.
     * <p>Ejemplo: {@code 3}</p>
     */
    private int activePets;

    /**
     * Texto descriptivo contextual para la métrica de mascotas activas.
     * <p>Ejemplo: {@code "Total de mascotas activas"}</p>
     */
    private String activePetsChange;

    /**
     * Número de citas o reservas futuras que están programadas y no han sido
     * completadas ni canceladas.
     * <p>Ejemplo: {@code 2}</p>
     */
    private int scheduledAppointments;

    /**
     * Texto descriptivo contextual para la métrica de citas programadas.
     * <p>Ejemplo: {@code "Próximas citas"}</p>
     */
    private String scheduledAppointmentsChange;

    /**
     * Estado de las vacunas de las mascotas, representado como una cadena de texto.
     * Usualmente en un formato como 'actualizadas/total'.
     * <p>Ejemplo: {@code "3/4"}</p>
     */
    private String vaccinesUpToDate;

    /**
     * Texto descriptivo contextual para la métrica de vacunas.
     * <p>Ejemplo: {@code "1 pendiente"}</p>
     */
    private String vaccinesChange;

    /**
     * Número de recordatorios o eventos importantes pendientes en un futuro cercano
     * (por ejemplo, citas en los próximos 7 días).
     * <p>Ejemplo: {@code 1}</p>
     */
    private int pendingReminders;

    /**
     * Texto descriptivo contextual para la métrica de recordatorios pendientes.
     * <p>Ejemplo: {@code "Evento próximo"}</p>
     */
    private String pendingRemindersChange;
}