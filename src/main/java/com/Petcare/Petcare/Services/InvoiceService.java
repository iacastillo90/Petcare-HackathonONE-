package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Invoice.CreateInvoiceRequest;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceSummaryResponse;
import com.Petcare.Petcare.DTOs.Invoice.UpdateInvoiceRequest;
import com.Petcare.Petcare.Models.Booking.Booking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz para el servicio de gestión de facturas (Invoices).
 *
 * <p>Define el contrato para las operaciones de negocio relacionadas con la
 * facturación, desde la generación automática a partir de reservas hasta
 * la consulta y gestión de su ciclo de vida.</p>
 *
 * @author Equipo Petcare
 * @version 1.0
 */
public interface InvoiceService {

    /**
     * Genera y guarda una nueva factura a partir de una reserva completada.
     *
     * @param request DTO que contiene la información para generar la factura.
     * @return DTO con los detalles completos de la factura recién creada.
     * @throws IllegalArgumentException si la reserva no se encuentra o no está en un estado válido.
     * @throws IllegalStateException si ya existe una factura para la reserva.
     */
    InvoiceDetailResponse generateInvoiceForBooking(CreateInvoiceRequest request);

    /**
     * Obtiene una factura específica por su ID.
     *
     * @param invoiceId El identificador único de la factura.
     * @return DTO con los detalles completos de la factura.
     * @throws IllegalArgumentException si no se encuentra ninguna factura con ese ID.
     */
    InvoiceDetailResponse getInvoiceById(Long invoiceId);

    /**
     * Obtiene una lista paginada de todas las facturas de una cuenta específica.
     *
     * @param accountId El ID de la cuenta para la que se buscan las facturas.
     * @param pageable La información de paginación.
     * @return Una página de DTOs con el resumen de las facturas de la cuenta.
     */
    Page<InvoiceSummaryResponse> getInvoicesByAccountId(Long accountId, Pageable pageable);

    InvoiceDetailResponse sendInvoice(@Positive Long invoiceId);

    InvoiceDetailResponse updateInvoice(@Positive Long invoiceId, @Valid UpdateInvoiceRequest request);

    InvoiceDetailResponse cancelInvoice(@Positive Long invoiceId, @NotBlank String reason);

    void generateAndProcessInvoiceForBooking(Booking booking);
}