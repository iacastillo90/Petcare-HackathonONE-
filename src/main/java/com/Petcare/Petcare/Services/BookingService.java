package com.Petcare.Petcare.Services;


import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.Models.User.User;

/**
 * Interfaz para el servicio de gestión de reservas (Bookings).
 * Define el contrato para las operaciones de negocio relacionadas con las reservas.
 */
public interface BookingService {

    /**
     * Crea una nueva reserva en el sistema.
     *
     * @param createBookingRequest DTO con los datos de entrada para la nueva reserva.
     * @param currentUser El usuario autenticado que está realizando la reserva.
     * @return Un DTO con los detalles de la reserva recién creada.
     */
    BookingDetailResponse createBooking(CreateBookingRequest createBookingRequest, User currentUser);

}