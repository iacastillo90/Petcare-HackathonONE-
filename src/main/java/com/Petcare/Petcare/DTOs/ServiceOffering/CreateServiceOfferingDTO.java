package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.sql.Time;

public record CreateServiceOfferingDTO(
        @NotNull(message = "El tipo de servicio es obligatorio")
        ServiceType serviceType,

        @NotBlank (message = "El nombre del servicio es obligatorio")
        @Size (min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String name,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String description,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin (value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999.99", message = "El precio no puede exceder 999.99")
        BigDecimal price,

        @NotNull(message = "La duración es obligatoria")
        Integer durationInMinutes
) {
}
