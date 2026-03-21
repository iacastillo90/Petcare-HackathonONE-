package com.Petcare.Petcare.DTOs.Invoice.InvoiceItem;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Información de items individuales de una factura.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "Información de items individuales de una factura.")
public record InvoiceItemResponse(
        @Schema(description = "Identificador único del item.", example = "1")
        Long id,

        @Schema(description = "Descripción del item.", example = "Paseo de 30 minutos")
        String description,

        @Schema(description = "Cantidad de unidades.", example = "2")
        Integer quantity,

        @Schema(description = "Precio unitario.", example = "15.00")
        BigDecimal unitPrice,

        @Schema(description = "Total de la línea (quantity * unitPrice).", example = "30.00")
        BigDecimal lineTotal
) {
}
