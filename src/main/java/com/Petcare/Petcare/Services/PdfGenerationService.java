package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;

/**
 * Interfaz para el servicio de generación de documentos PDF.
 */
public interface PdfGenerationService {

    /**
     * Genera un archivo PDF para una factura específica.
     * @param invoice DTO con los detalles completos de la factura.
     * @return un array de bytes que representa el archivo PDF.
     */
    byte[] generateInvoicePdf(InvoiceDetailResponse invoice);
}