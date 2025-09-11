package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

public record ServiceOfferingDTO(
        Long id,
        Long sitterId,
        ServiceType serviceType,
        String name,
        String description,
        BigDecimal price,
        Integer durationInMinutes,
        boolean isActive,
        Timestamp createdAt
) {
    public ServiceOfferingDTO( ServiceOffering serviceOffering) {
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