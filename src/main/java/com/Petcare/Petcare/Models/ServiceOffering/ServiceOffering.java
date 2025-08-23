package com.Petcare.Petcare.Models.ServiceOffering;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "service_offering")
public class ServiceOffering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "service_offering_id")
    private Long sitterId;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private String name;
    private String description;
    private BigDecimal price;
    private Time durationInMinutes;
    private boolean isActive;
    private Timestamp createdAt;
}
