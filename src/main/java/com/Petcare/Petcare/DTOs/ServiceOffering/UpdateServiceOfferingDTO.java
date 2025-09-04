package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

public record UpdateServiceOfferingDTO(
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
}