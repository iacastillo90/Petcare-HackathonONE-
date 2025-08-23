package com.Petcare.Petcare.DTOs.ServiceOffering;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

public record ServiceOfferingDTO(
        Long id,
        Long serviceOfferingId,
        ServiceType serviceType,
        String name,
        String description,
        BigDecimal price,
        Time durationInMinutes,
        boolean isActive,
        Timestamp createdAt
) {
}
