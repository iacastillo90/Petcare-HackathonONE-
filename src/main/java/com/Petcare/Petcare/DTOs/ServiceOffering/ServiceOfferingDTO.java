package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Controllers.ServiceOfferingController;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para representar una oferta de servicio en las respuestas de la API.
 * <p>
 * Este objeto inmutable ({@code record}) proporciona una representación segura y estructurada de la
 * entidad {@link ServiceOffering}. Se utiliza como el cuerpo de la respuesta en los endpoints
 * que devuelven información sobre uno o varios servicios, desacoplando la capa de la API de la
 * capa de persistencia.
 *
 * @see ServiceOfferingController
 * @see ServiceOffering
 *
 * @param id El identificador único de la oferta de servicio.
 * @param sitterId El ID del <strong>usuario</strong> (User) que ofrece el servicio.
 * @param serviceType El tipo de servicio (ej. PASEO, GUARDERÍA).
 * @param name El nombre público y descriptivo del servicio.
 * @param description Un texto detallado sobre lo que incluye el servicio.
 * @param price La tarifa del servicio.
 * @param durationInMinutes La duración total del servicio en minutos.
 * @param isActive Indica si la oferta de servicio está actualmente activa y puede ser reservada.
 * @param createdAt La fecha y hora en que se creó la oferta de servicio.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see ServiceOffering
 */
@Schema(description = "DTO que representa una oferta de servicio en las respuestas de la API.")
public record ServiceOfferingDTO(

        @Schema(description = "Identificador único de la oferta de servicio.", example = "1")
        Long id,

        @Schema(description = "ID del usuario (cuidador) que ofrece el servicio.", example = "15")
        Long sitterId,

        @Schema(description = "Tipo de servicio ofrecido.", example = "WALKING")
        ServiceType serviceType,

        @Schema(description = "Nombre público del servicio.", example = "Paseo Energético de 60 Minutos")
        String name,

        @Schema(description = "Descripción detallada de lo que incluye el servicio.", example = "Un paseo vigoroso de una hora por parques locales para perros con mucha energía.")
        String description,

        @Schema(description = "Precio del servicio.", example = "20.00")
        BigDecimal price,

        @Schema(description = "Duración total del servicio en minutos.", example = "60")
        Integer durationInMinutes,

        @Schema(description = "Indica si el servicio está activo y disponible para ser reservado.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora de creación de la oferta de servicio.", example = "2025-09-10T09:00:00")
        LocalDateTime createdAt
) {
    /**
     * Constructor de conveniencia para mapear una entidad {@link ServiceOffering} a este DTO.
     * <p>
     * Este es el mecanismo preferido para crear instancias de {@code ServiceOfferingDTO},
     * ya que centraliza la lógica de mapeo y asegura una conversión consistente desde
     * la capa de persistencia.
     *
     * @param serviceOffering La entidad {@link ServiceOffering} de origen. No debe ser nula.
     */
    public ServiceOfferingDTO(ServiceOffering serviceOffering) {
        this(
                serviceOffering.getId(),
                serviceOffering.getSitterId(),
                serviceOffering.getServiceType(),
                serviceOffering.getName(),
                serviceOffering.getDescription(),
                serviceOffering.getPrice(),
                serviceOffering.getDurationInMinutes(),
                serviceOffering.isActive(),
                serviceOffering.getCreatedAt()
        );
    }
}