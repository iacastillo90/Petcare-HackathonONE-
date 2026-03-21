package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DTO para transferir información de ofertas de servicios de cuidado de mascotas.
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see ServiceOffering
 */
@Schema(description = "DTO para transferir información de ofertas de servicios de cuidado de mascotas.")
public record ServiceOfferingDTO(
        @Schema(description = "Identificador único de la oferta de servicio.", example = "1")
        Long id,

        @Schema(description = "ID del cuidador que ofrece el servicio.", example = "2")
        Long sitterId,

        @Schema(description = "Tipo de servicio ofrecido.", example = "WALKING")
        ServiceType serviceType,

        @Schema(description = "Nombre descriptivo del servicio.", example = "Paseo de 30 minutos")
        String name,

        @Schema(description = "Descripción detallada del servicio.", example = "Paseo agradable por el parque")
        String description,

        @Schema(description = "Precio del servicio.", example = "25.00")
        BigDecimal price,

        @Schema(description = "Duración del servicio en minutos.", example = "30")
        Integer durationInMinutes,

        @Schema(description = "Indica si el servicio está activo y disponible.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora de creación del servicio.", example = "2025-02-10T15:30:00Z")
        Timestamp createdAt
) {

    /**
     * Constructor para crear DTO desde entidad ServiceOffering.
     *
     * @param serviceOffering la entidad ServiceOffering a convertir.
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

    /**
     * Convierte una entidad del modelo {@link ServiceOffering} a un DTO {@link ServiceOfferingDTO}.
     *
     * <p>Este método de fábrica estático es la forma recomendada para crear instancias de {@code ServiceOfferingDTO}.</p>
     *
     * @param serviceOffering la entidad ServiceOffering a convertir.
     * @return nueva instancia de ServiceOfferingDTO con datos poblados.
     */
    public static ServiceOfferingDTO fromEntity(ServiceOffering serviceOffering) {
        if (serviceOffering == null) {
            throw new IllegalArgumentException("La entidad ServiceOffering no puede ser null");
        }
        return new ServiceOfferingDTO(serviceOffering);
    }

    /**
     * Verifica si el servicio está activo.
     *
     * @return true si el servicio está activo.
     */
    public boolean isActive() {
        return isActive;
    }
}
