package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.Models.Booking.Booking;

/**
 * Servicio para el manejo de notificaciones relacionadas con reservas.
 */
public interface NotificationService {

    /**
     * Notifica la creación de una nueva reserva a las partes involucradas.
     */
    void notifyNewBookingCreated(Booking booking);

    /**
     * Notifica cambios de estado en una reserva.
     */
    void notifyStatusChange(Booking booking);

    /**
     * Notifica actualizaciones en una reserva.
     */
    void notifyBookingUpdated(Booking booking);

    /**
     * Notifica la cancelación de una reserva.
     */
    void notifyBookingCancelled(Booking booking);
}
