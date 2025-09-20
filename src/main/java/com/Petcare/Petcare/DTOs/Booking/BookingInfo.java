package com.Petcare.Petcare.DTOs.Booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Información resumida de la reserva.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfo {
    private Long id;
    private String petName;
    private String serviceName;
    private String sitterName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

