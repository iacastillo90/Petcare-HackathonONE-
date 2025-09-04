package com.Petcare.Petcare.DTOs.Invoice.InvoiceItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Información de items individuales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponse {
    private Long id;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}