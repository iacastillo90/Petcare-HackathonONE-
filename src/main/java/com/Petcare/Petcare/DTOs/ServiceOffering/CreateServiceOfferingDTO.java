package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Controllers.ServiceOfferingController;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la creación de una nueva oferta de servicio.
 * <p>
 * Este objeto inmutable ({@code record}) encapsula todos los datos que un cuidador debe
 * proporcionar para registrar un nuevo servicio en su catálogo. Se utiliza como cuerpo
 * de la solicitud en el endpoint de creación y aplica validaciones de negocio a nivel de
 * controlador para garantizar la integridad de los datos antes de que lleguen a la capa de servicio.
 *
 * @see ServiceOfferingController#createServiceOffering(CreateServiceOfferingDTO, Long)
 * @see ServiceOffering
 *
 * @param serviceType El tipo de servicio ofrecido (ej. PASEO, GUARDERÍA). Es un campo obligatorio.
 * @param name El nombre público y descriptivo del servicio. Debe ser único para el cuidador.
 * @param description Un texto detallado que explica en qué consiste el servicio, qué incluye y cualquier otra información relevante para el cliente.
 * @param price La tarifa del servicio. Debe ser un valor positivo.
 * @param durationInMinutes La duración total del servicio expresada en minutos. Debe ser de al menos 15 minutos.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see ServiceOffering
 */
@Schema(description = "DTO con los datos necesarios para crear una nueva oferta de servicio en el catálogo de un cuidador.")
public record CreateServiceOfferingDTO(

        @Schema(description = "Tipo de servicio ofrecido. Es un campo obligatorio.", example = "WALKING")
        @NotNull(message = "El tipo de servicio es obligatorio")
        ServiceType serviceType,

        @Schema(description = "Nombre único y descriptivo para el servicio.", example = "Paseo Energético de 60 Minutos")
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String name,

        @Schema(description = "Descripción detallada de lo que incluye el servicio.", example = "Un paseo vigoroso de una hora por parques locales para perros con mucha energía.")
        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String description,

        @Schema(description = "Precio del servicio. Debe ser un valor positivo.", example = "20.00")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999.99", message = "El precio no puede exceder 999.99")
        BigDecimal price,

        @Schema(description = "Duración total del servicio en minutos. El mínimo es de 15 minutos.", example = "60")
        @NotNull(message = "La duración es obligatoria")
        @Min(value = 15, message = "La duración mínima del servicio es 15 minutos")
        Integer durationInMinutes
) {
}