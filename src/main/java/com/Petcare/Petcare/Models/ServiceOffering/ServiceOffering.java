package com.Petcare.Petcare.Models.ServiceOffering;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Entidad que representa una oferta de servicio en la plataforma Petcare.
 * 
 * Esta clase modela los servicios que los cuidadores (sitters) pueden ofrecer
 * a los dueños de mascotas, incluyendo información sobre el tipo de servicio,
 * precio, duración y disponibilidad.
 * 
 * @author Jorge
 * @version 1.0
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "service_offering")
public class ServiceOffering {
    
    /**
     * Identificador único de la oferta de servicio.
     * Se genera automáticamente usando estrategia de identidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del cuidador que ofrece el servicio.
     * Referencia al ID del sitter en la tabla correspondiente.
     */
    @JoinColumn(name = "service_offering_id")
    private Long sitterId;

    /**
     * Tipo de servicio ofrecido (ej: paseo, cuidado, etc.).
     * Se almacena como string en la base de datos.
     */
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    /**
     * Nombre descriptivo del servicio ofrecido.
     */
    private String name;
    
    /**
     * Descripción detallada del servicio, incluyendo
     * características específicas y condiciones.
     */
    private String description;
    
    /**
     * Precio del servicio en la moneda local.
     * Utiliza BigDecimal para precisión en cálculos monetarios.
     */
    private BigDecimal price;
    
    /**
     * Duración estimada del servicio en minutos.
     */
    private Time durationInMinutes;
    
    /**
     * Indica si el servicio está actualmente disponible.
     * true = activo, false = inactivo/suspendido.
     */
    private boolean isActive;
    
    /**
     * Timestamp de creación del registro.
     * Se establece automáticamente al crear la oferta.
     */
    private Timestamp createdAt;
}
