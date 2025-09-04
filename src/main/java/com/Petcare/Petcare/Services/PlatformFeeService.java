package com.Petcare.Petcare.Services;


import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.PlatformFee;

public interface PlatformFeeService {
    /**
     * Calcula y crea una nueva tarifa de plataforma para una reserva.
     * @param request DTO con el ID de la reserva y el porcentaje de la tarifa.
     * @return DTO con los detalles de la tarifa recién creada.
     */
    PlatformFeeResponse calculateAndCreateFee(CreatePlatformFeeRequest request);


    /**
     * Calcula y crea las tarifas de plataforma para una reserva.
     */
    PlatformFee calculateAndCreatePlatformFee(Booking booking);

    /**
     * Recalcula las tarifas cuando cambia el precio de la reserva.
     */
    void recalculatePlatformFee(Booking booking);

    void calculateAndCreateFee(Invoice savedInvoice);
}