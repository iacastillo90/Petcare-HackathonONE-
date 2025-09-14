package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Controllers.ServiceOfferingController;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la actualización de una oferta de servicio existente.
 * <p>
 * Este objeto inmutable ({@code record}) define la estructura de datos que un cliente debe
 * enviar para modificar un servicio existente. Contiene únicamente los campos que son
 * susceptibles de ser modificados por un usuario (cuidador o administrador).
 * </p>
 * <p>
 * Campos como {@code id}, {@code sitterId} y {@code createdAt} se excluyen deliberadamente,
 * ya que el ID del recurso se especifica en la URL del endpoint y los otros son
 * valores inmutables o gestionados por el sistema.
 * </p>
 *
 * @see ServiceOfferingController#updateService(Long, UpdateServiceOfferingDTO)
 * @see ServiceOffering
 *
 * @param serviceType El nuevo tipo de servicio (ej. PASEO, GUARDERÍA).
 * @param name El nuevo nombre para el servicio. Debe seguir siendo único para el cuidador.
 * @param description La nueva descripción detallada del servicio.
 * @param price La nueva tarifa del servicio.
 * @param durationInMinutes La nueva duración del servicio en minutos.
 * @param isActive El nuevo estado de activación del servicio (true para activarlo, false para desactivarlo).
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see ServiceOffering
 */
@Schema(description = "DTO con los campos modificables de una oferta de servicio. Se utiliza como cuerpo de la solicitud en las operaciones de actualización.")
public record UpdateServiceOfferingDTO(

        @Schema(description = "El nuevo tipo de servicio.", example = "SITTING")
        @NotNull(message = "El tipo de servicio es obligatorio")
        ServiceType serviceType,

        @Schema(description = "El nuevo nombre para el servicio.", example = "Cuidado a Domicilio Premium (3 horas)")
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String name,

        @Schema(description = "La nueva descripción detallada del servicio.", example = "Cuidado y compañía para tu mascota en la comodidad de tu hogar, incluyendo juegos y alimentación.")
        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String description,

        @Schema(description = "La nueva tarifa del servicio.", example = "55.00")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999.99", message = "El precio no puede exceder 999.99")
        BigDecimal price,

        @Schema(description = "La nueva duración del servicio en minutos.", example = "180")
        @NotNull(message = "La duración es obligatoria")
        @Min(value = 15, message = "La duración mínima del servicio es 15 minutos")
        Integer durationInMinutes,

        @Schema(description = "El nuevo estado de activación del servicio. `true` para activo, `false` para inactivo.", example = "true")
        @NotNull(message = "El estado de activación es obligatorio")
        Boolean isActive
) {
}