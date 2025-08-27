package com.Petcare.Petcare.Services;


import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;

public interface PlatformFeeService {
    /**
     * Calcula y crea una nueva tarifa de plataforma para una reserva.
     * @param request DTO con el ID de la reserva y el porcentaje de la tarifa.
     * @return DTO con los detalles de la tarifa recién creada.
     */
    PlatformFeeResponse calculateAndCreateFee(CreatePlatformFeeRequest request);
}