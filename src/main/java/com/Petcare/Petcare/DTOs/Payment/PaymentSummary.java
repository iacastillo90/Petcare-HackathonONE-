package com.Petcare.Petcare.DTOs.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Información resumida de pagos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummary {
    private Long id;
    private BigDecimal amount;
    private String status;
    private LocalDateTime processedAt;
}

