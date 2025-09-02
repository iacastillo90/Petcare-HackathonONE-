package com.Petcare.Petcare.DTOs.User;

import lombok.Builder;
import lombok.Data;

/**
 * DTO para estadísticas generales de usuarios del sistema.
 *
 * <p>Proporciona métricas agregadas sobre la base de usuarios, útil para
 * dashboards administrativos y reportes de gestión.</p>
 *
 * <p><strong>Métricas incluidas:</strong></p>
 * <ul>
 *   <li>Conteo total y activo de usuarios</li>
 *   <li>Distribución por roles del sistema</li>
 *   <li>Estado de verificación de emails</li>
 *   <li>Cálculos de porcentajes para análisis</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Dashboards administrativos</li>
 *   <li>Reportes ejecutivos de crecimiento</li>
 *   <li>Métricas de engagement de usuarios</li>
 *   <li>Análisis de adopción por roles</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class UserStatsResponse {

    /**
     * Total de usuarios registrados en el sistema.
     */
    private long totalUsers;

    /**
     * Número de usuarios con cuenta activa.
     */
    private long activeUsers;

    /**
     * Número de usuarios con rol CLIENT.
     */
    private long clientCount;

    /**
     * Número de usuarios con rol SITTER.
     */
    private long sitterCount;

    /**
     * Número de usuarios con rol ADMIN.
     */
    private long adminCount;

    /**
     * Número de usuarios con email verificado.
     */
    private long verifiedUsers;

    // ========== MÉTODOS DE CÁLCULO DE PORCENTAJES ==========

    /**
     * Calcula el porcentaje de usuarios activos.
     *
     * @return porcentaje de usuarios activos (0-100)
     */
    public double getActiveUsersPercentage() {
        return totalUsers == 0 ? 0 : (double) activeUsers / totalUsers * 100;
    }

    /**
     * Calcula el porcentaje de emails verificados.
     *
     * @return porcentaje de emails verificados (0-100)
     */
    public double getVerifiedUsersPercentage() {
        return totalUsers == 0 ? 0 : (double) verifiedUsers / totalUsers * 100;
    }

    /**
     * Calcula el porcentaje de usuarios con rol CLIENT.
     *
     * @return porcentaje de clientes (0-100)
     */
    public double getClientPercentage() {
        return totalUsers == 0 ? 0 : (double) clientCount / totalUsers * 100;
    }

    /**
     * Calcula el porcentaje de usuarios con rol SITTER.
     *
     * @return porcentaje de cuidadores (0-100)
     */
    public double getSitterPercentage() {
        return totalUsers == 0 ? 0 : (double) sitterCount / totalUsers * 100;
    }

    /**
     * Calcula el porcentaje de usuarios con rol ADMIN.
     *
     * @return porcentaje de administradores (0-100)
     */
    public double getAdminPercentage() {
        return totalUsers == 0 ? 0 : (double) adminCount / totalUsers * 100;
    }

    // ========== MÉTODOS DE ANÁLISIS ==========

    /**
     * Determina si el sistema tiene una distribución saludable de roles.
     * Se considera saludable si hay al menos 1 admin y la proporción de
     * sitters vs clients está entre 10% y 90%.
     *
     * @return true si la distribución de roles es saludable
     */
    public boolean hasHealthyRoleDistribution() {
        if (adminCount == 0) return false;

        long serviceUsers = clientCount + sitterCount;
        if (serviceUsers == 0) return totalUsers <= 1; // Solo admins

        double sitterRatio = (double) sitterCount / serviceUsers;
        return sitterRatio >= 0.1 && sitterRatio <= 0.9;
    }

    /**
     * Calcula la tasa de verificación de email como indicador de engagement.
     *
     * @return descripción textual de la tasa de verificación
     */
    public String getVerificationStatus() {
        double percentage = getVerifiedUsersPercentage();

        if (percentage >= 90) return "Excelente";
        if (percentage >= 75) return "Buena";
        if (percentage >= 50) return "Regular";
        if (percentage >= 25) return "Baja";
        return "Crítica";
    }

    /**
     * Identifica el rol más común en el sistema.
     *
     * @return string con el rol predominante
     */
    public String getDominantRole() {
        if (clientCount > sitterCount && clientCount > adminCount) {
            return "Cliente";
        } else if (sitterCount > clientCount && sitterCount > adminCount) {
            return "Cuidador";
        } else if (adminCount > clientCount && adminCount > sitterCount) {
            return "Administrador";
        } else {
            return "Equilibrado";
        }
    }

    /**
     * Calcula el número de usuarios inactivos.
     *
     * @return número de usuarios inactivos
     */
    public long getInactiveUsers() {
        return Math.max(0, totalUsers - activeUsers);
    }

    /**
     * Calcula el número de usuarios con email sin verificar.
     *
     * @return número de usuarios sin verificar email
     */
    public long getUnverifiedUsers() {
        return Math.max(0, totalUsers - verifiedUsers);
    }

    // ========== MÉTODOS DE UTILIDAD PARA UI ==========

    /**
     * Genera un resumen textual de las estadísticas principales.
     *
     * @return string con resumen ejecutivo
     */
    public String getExecutiveSummary() {
        return String.format(
                "Total: %d usuarios | Activos: %d (%.1f%%) | Verificados: %d (%.1f%%) | Rol dominante: %s",
                totalUsers,
                activeUsers, getActiveUsersPercentage(),
                verifiedUsers, getVerifiedUsersPercentage(),
                getDominantRole()
        );
    }

    /**
     * Proporciona recomendaciones basadas en las métricas actuales.
     *
     * @return lista de recomendaciones para mejorar métricas
     */
    public String getRecommendations() {
        StringBuilder recommendations = new StringBuilder();

        if (getVerifiedUsersPercentage() < 50) {
            recommendations.append("• Implementar campañas de verificación de email. ");
        }

        if (getActiveUsersPercentage() < 70) {
            recommendations.append("• Revisar estrategias de retención de usuarios. ");
        }

        if (!hasHealthyRoleDistribution() && totalUsers > 1) {
            recommendations.append("• Balancear la adquisición de clientes y cuidadores. ");
        }

        if (adminCount == 0) {
            recommendations.append("• Asignar al menos un administrador al sistema. ");
        }

        return recommendations.length() > 0 ? recommendations.toString() : "Las métricas están dentro de rangos saludables.";
    }

    @Override
    public String toString() {
        return "UserStatsResponse{" +
                "totalUsers=" + totalUsers +
                ", activeUsers=" + activeUsers + " (" + String.format("%.1f", getActiveUsersPercentage()) + "%)" +
                ", clientCount=" + clientCount +
                ", sitterCount=" + sitterCount +
                ", adminCount=" + adminCount +
                ", verifiedUsers=" + verifiedUsers + " (" + String.format("%.1f", getVerifiedUsersPercentage()) + "%)" +
                ", dominantRole='" + getDominantRole() + '\'' +
                '}';
    }
}