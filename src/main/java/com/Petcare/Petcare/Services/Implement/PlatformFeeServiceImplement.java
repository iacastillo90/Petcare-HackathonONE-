package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.PlatformFee;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.PlatformFeeRepository;
import com.Petcare.Petcare.Services.PlatformFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PlatformFeeServiceImplement implements PlatformFeeService {

    private final PlatformFeeRepository platformFeeRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public PlatformFeeResponse calculateAndCreateFee(CreatePlatformFeeRequest request) {

        // 1. Validar la entrada y buscar la reserva
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + request.getBookingId()));

        // 2. Lógica de negocio: Calcular los montos
        BigDecimal baseAmount = booking.getTotalPrice();
        BigDecimal feePercentage = request.getFeePercentage().divide(new BigDecimal("100"));

        BigDecimal feeAmount = baseAmount.multiply(feePercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = baseAmount.subtract(feeAmount);

        // 3. Crear y guardar la nueva entidad PlatformFee
        PlatformFee newFee = new PlatformFee();
        newFee.setBooking(booking);
        newFee.setBaseAmount(baseAmount);
        newFee.setFeePercentage(request.getFeePercentage());
        newFee.setFeeAmount(feeAmount);
        newFee.setNetAmount(netAmount);

        PlatformFee savedFee = platformFeeRepository.save(newFee);

        // 4. Devolver la respuesta en formato DTO
        return PlatformFeeResponse.fromEntity(savedFee);
    }

    @Override
    public PlatformFee calculateAndCreatePlatformFee(Booking booking) {
        return null;
    }

    @Override
    public void recalculatePlatformFee(Booking booking) {

    }

    @Override
    public void calculateAndCreateFee(Invoice savedInvoice) {
        
    }


}