package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para agregar y presentar estadísticas clave sobre la base de usuarios del sistema.
 * <p>
 * Este objeto está diseñado para ser una respuesta de solo lectura que proporciona una instantánea
 * del estado y la salud del ecosistema de usuarios en la plataforma Petcare. Su propósito principal
 * es alimentar dashboards administrativos, generar reportes de negocio y facilitar el monitoreo
 * del crecimiento y la actividad de los usuarios.
 * </p>
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see User
 */
@Schema(description = "Agregado de métricas y estadísticas clave sobre la base de usuarios, diseñado para dashboards y reportes administrativos.")
public record UserStatsResponse(
        @Schema(description = "Conteo total de usuarios registrados en el sistema, sin importar su estado.", example = "1520")
        long totalUsers,

        @Schema(description = "Número de usuarios cuya cuenta está marcada como activa y pueden iniciar sesión.", example = "1450")
        long activeUsers,

        @Schema(description = "Conteo de usuarios con el rol específico de CLIENTE.", example = "1200")
        long clientCount,

        @Schema(description = "Conteo de usuarios con el rol específico de CUIDADOR (SITTER).", example = "245")
        long sitterCount,

        @Schema(description = "Conteo de usuarios con el rol específico de ADMINISTRADOR.", example = "5")
        long adminCount,

        @Schema(description = "Número de usuarios que han verificado su dirección de correo electrónico.", example = "1380")
        long verifiedUsers
) {

    /**
     * Calcula y devuelve el porcentaje de usuarios activos sobre el total.
     *
     * @return Porcentaje de usuarios activos (de 0 a 100).
     */
    public double activeUsersPercentage() {
        if (totalUsers == 0) return 0;
        return (double) activeUsers / totalUsers * 100;
    }

    /**
     * Calcula y devuelve el porcentaje de usuarios que han verificado su email sobre el total.
     *
     * @return Porcentaje de usuarios verificados (de 0 a 100).
     */
    public double verifiedUsersPercentage() {
        if (totalUsers == 0) return 0;
        return (double) verifiedUsers / totalUsers * 100;
    }

    /**
     * Genera un resumen textual de las estadísticas principales para visualización rápida o logs.
     *
     * @return Una cadena con el resumen ejecutivo de las métricas.
     */
    public String executiveSummary() {
        return String.format(
                "Total: %d usuarios | Activos: %d (%.1f%%) | Verificados: %d (%.1f%%)",
                totalUsers,
                activeUsers, activeUsersPercentage(),
                verifiedUsers, verifiedUsersPercentage()
        );
    }
}
