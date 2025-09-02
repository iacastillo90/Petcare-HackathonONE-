package com.Petcare.Petcare.Services.Implement;


import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImplement implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImplement.class);

    @Override
    public void notifyNewBookingCreated(Booking booking) {
        logger.info("SIMULATING NOTIFICATION: Nueva reserva #{} creada para el cuidador {}.",
                booking.getId(), booking.getSitter().getFirstName());
    }

    @Override
    public void notifyStatusChange(Booking booking) {
        logger.info("SIMULATING NOTIFICATION: El estado de la reserva #{} ha cambiado a {}.",
                booking.getId(), booking.getStatus());
    }

    @Override
    public void notifyBookingUpdated(Booking booking) {
        logger.info("SIMULATING NOTIFICATION: La reserva #{} ha sido actualizada.", booking.getId());
    }

    @Override
    public void notifyBookingCancelled(Booking booking) {
        logger.info("SIMULATING NOTIFICATION: La reserva #{} ha sido cancelada.", booking.getId());
    }
}