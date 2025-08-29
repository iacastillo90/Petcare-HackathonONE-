package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar las operaciones de reservas (Bookings).
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Endpoint para crear una nueva reserva.
     *
     * @param request DTO con los datos para la creación de la reserva.
     * @param currentUser El usuario autenticado que realiza la petición.
     * @return Un ResponseEntity con el DTO de la reserva creada y un estado 201 Created.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        BookingDetailResponse newBooking = bookingService.createBooking(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

}