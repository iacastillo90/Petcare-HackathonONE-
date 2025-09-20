package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Invoice.Invoice;

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

    void sendInvoiceEmail(Invoice invoice, byte[] pdfBytes);

    void notifyInvoiceSent(Invoice invoice);

    void notifyInvoiceCancelled(Invoice invoice, String reason);

    void notifyInvoiceUpdated(Invoice invoice);

    void notifyInvoiceGenerated(Invoice savedInvoice);
}
