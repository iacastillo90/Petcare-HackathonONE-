package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * DTO para las respuestas de estadísticas de mascotas.
 *
 * <p>Proporciona información agregada y métricas sobre las mascotas registradas
 * en el sistema, útil para dashboards administrativos y reportes de gestión.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Pet
 */
@Schema(description = "DTO para las respuestas de estadísticas de mascotas.")
public record PetStatsResponse(
        @Schema(description = "Número total de mascotas registradas.", example = "150")
        long totalPets,

        @Schema(description = "Número de mascotas activas.", example = "120")
        long activePets,

        @Schema(description = "Número de mascotas inactivas.", example = "30")
        long inactivePets,

        @Schema(description = "Distribución de mascotas por especie.")
        Map<String, Long> petsBySpecies,

        @Schema(description = "Distribución de mascotas por género.")
        Map<String, Long> petsByGender,

        @Schema(description = "Distribución de mascotas por rango de edad.")
        Map<String, Long> petsByAgeRange,

        @Schema(description = "Número total de cuentas con mascotas.", example = "80")
        long accountsWithPets,

        @Schema(description = "Promedio de mascotas por cuenta.", example = "1.87")
        double averagePetsPerAccount,

        @Schema(description = "Número de mascotas registradas en los últimos 30 días.", example = "12")
        long petsRegisteredLast30Days,

        @Schema(description = "Número de mascotas registradas en los últimos 7 días.", example = "3")
        long petsRegisteredLast7Days
) {
}
