package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para agregar y presentar estadísticas clave sobre la base de usuarios del sistema.
 * <p>
 * Este objeto está diseñado para ser una respuesta de solo lectura que proporciona una instantánea
 * del estado y la salud del ecosistema de usuarios en la plataforma Petcare. Su propósito principal
 * es alimentar dashboards administrativos, generar reportes de negocio y facilitar el monitoreo
 * del crecimiento y la actividad de los usuarios.
 * </p>
 * <p>
 * <strong>Contenido y Métricas:</strong>
 * </p>
 * <ul>
 * <li><b>Conteos Totales:</b> Ofrece una visión general del tamaño de la base de usuarios.</li>
 * <li><b>Distribución por Rol:</b> Desglosa la composición de la comunidad (clientes, cuidadores, administradores).</li>
 * <li><b>Indicadores de Salud:</b> Mide la actividad y el compromiso a través de métricas como usuarios activos y verificados.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see User
 * @see com.Petcare.Petcare.Controllers.UserController#getUserStats()
 */
@Data
@Builder
@Schema(description = "Agregado de métricas y estadísticas clave sobre la base de usuarios, diseñado para dashboards y reportes administrativos.")
public class UserStatsResponse {

    /**
     * Conteo total de usuarios registrados en el sistema, sin importar su estado (activos o inactivos).
     */
    @Schema(description = "Conteo total de usuarios registrados en el sistema, sin importar su estado.", example = "1520")
    private long totalUsers;

    /**
     * Número de usuarios cuya cuenta está marcada como activa (`isActive = true`) y pueden iniciar sesión.
     */
    @Schema(description = "Número de usuarios cuya cuenta está marcada como activa y pueden iniciar sesión.", example = "1450")
    private long activeUsers;

    /**
     * Conteo de usuarios con el rol específico de CLIENTE.
     */
    @Schema(description = "Conteo de usuarios con el rol específico de CLIENTE.", example = "1200")
    private long clientCount;

    /**
     * Conteo de usuarios con el rol específico de CUIDADOR (SITTER).
     */
    @Schema(description = "Conteo de usuarios con el rol específico de CUIDADOR (SITTER).", example = "245")
    private long sitterCount;

    /**
     * Conteo de usuarios con el rol específico de ADMINISTRADOR.
     */
    @Schema(description = "Conteo de usuarios con el rol específico de ADMINISTRADOR.", example = "5")
    private long adminCount;

    /**
     * Número de usuarios que han verificado su dirección de correo electrónico (`emailVerifiedAt` no es nulo).
     */
    @Schema(description = "Número de usuarios que han verificado su dirección de correo electrónico.", example = "1380")
    private long verifiedUsers;

    // ========== MÉTODOS DE CÁLCULO (NO SERIALIZADOS) ==========

    /**
     * Calcula y devuelve el porcentaje de usuarios activos sobre el total.
     * <p>
     * Este método es una utilidad de lógica de negocio y no forma parte del contrato JSON de la API.
     * </p>
     * @return Porcentaje de usuarios activos (de 0 a 100).
     */
    @JsonIgnore
    public double getActiveUsersPercentage() {
        if (totalUsers == 0) return 0;
        return (double) activeUsers / totalUsers * 100;
    }

    /**
     * Calcula y devuelve el porcentaje de usuarios que han verificado su email sobre el total.
     * <p>
     * Este método es una utilidad de lógica de negocio y no forma parte del contrato JSON de la API.
     * </p>
     * @return Porcentaje de usuarios verificados (de 0 a 100).
     */
    @JsonIgnore
    public double getVerifiedUsersPercentage() {
        if (totalUsers == 0) return 0;
        return (double) verifiedUsers / totalUsers * 100;
    }

    /**
     * Genera un resumen textual de las estadísticas principales para visualización rápida o logs.
     * <p>
     * Este método es una utilidad de lógica de negocio y no forma parte del contrato JSON de la API.
     * </p>
     * @return Una cadena con el resumen ejecutivo de las métricas.
     */
    @JsonIgnore
    public String getExecutiveSummary() {
        return String.format(
                "Total: %d usuarios | Activos: %d (%.1f%%) | Verificados: %d (%.1f%%)",
                totalUsers,
                activeUsers, getActiveUsersPercentage(),
                verifiedUsers, getVerifiedUsersPercentage()
        );
    }
}