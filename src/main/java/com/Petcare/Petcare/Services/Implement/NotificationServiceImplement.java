package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Email.Attachment;
import com.Petcare.Petcare.DTOs.Email.Email;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Services.BookingService;
import com.Petcare.Petcare.Services.EmailService;
import com.Petcare.Petcare.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImplement implements NotificationService {

    private final EmailService emailService;

    // ========== Notificaciones de Reservas (Booking) ==========

    @Override
    @Async
    public void notifyNewBookingCreated(Booking booking) {
        log.info("Procesando notificaciones para nueva reserva ID: {}", booking.getId());
        // Notificar tanto al cliente como al cuidador
        sendNewBookingNotificationToClient(booking);
        sendNewBookingNotificationToSitter(booking);
    }

    @Override
    @Async
    public void notifyStatusChange(Booking booking) {
        log.info("Procesando notificación de cambio de estado para reserva ID: {} a {}",
                booking.getId(), booking.getStatus());

        String subject;
        String templateName;

        switch (booking.getStatus()) {
            case CONFIRMED:
                subject = "¡Tu reserva ha sido confirmada!";
                templateName = "booking-status-confirmed";
                break;
            case COMPLETED:
                subject = "El servicio para tu mascota ha finalizado";
                templateName = "booking-status-completed";
                break;
            case CANCELLED:
                // Usamos el método específico para cancelaciones para más detalle
                notifyBookingCancelled(booking);
                return;
            default:
                log.warn("No hay notificación definida para el estado de reserva: {}", booking.getStatus());
                return;
        }

        try {
            Map<String, Object> variables = createBaseBookingVariables(booking);

            Email email = Email.builder()
                    .to(booking.getBookedByUser().getEmail())
                    .subject(subject)
                    .templateName(templateName)
                    .variables(variables)
                    .build();

            emailService.sendEmail(email);
        } catch (Exception e) {
            log.error("Error al enviar notificación de cambio de estado para reserva {}: {}",
                    booking.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void notifyBookingUpdated(Booking booking) {
        log.info("Procesando notificación de actualización para reserva ID: {}", booking.getId());
        try {
            Map<String, Object> variables = createBaseBookingVariables(booking);
            variables.put("updateDetails", "Se han realizado cambios en los detalles de la reserva.");

            Email email = Email.builder()
                    .to(booking.getBookedByUser().getEmail())
                    .subject("Actualización sobre tu reserva #" + booking.getId())
                    .templateName("booking-updated")
                    .variables(variables)
                    .build();

            emailService.sendEmail(email);
        } catch (Exception e) {
            log.error("Error al enviar notificación de actualización para reserva {}: {}",
                    booking.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void notifyBookingCancelled(Booking booking) {
        log.info("Procesando notificación de cancelación para reserva ID: {}", booking.getId());
        try {
            Map<String, Object> variables = createBaseBookingVariables(booking);
            variables.put("cancellationReason", booking.getCancellationReason());

            // Notificar al cliente
            Email clientEmail = Email.builder()
                    .to(booking.getBookedByUser().getEmail())
                    .subject("Tu reserva #" + booking.getId() + " ha sido cancelada")
                    .templateName("booking-cancelled-client")
                    .variables(variables)
                    .build();
            emailService.sendEmail(clientEmail);

            // Notificar al cuidador
            Email sitterEmail = Email.builder()
                    .to(booking.getSitter().getEmail())
                    .subject("La reserva #" + booking.getId() + " ha sido cancelada")
                    .templateName("booking-cancelled-sitter")
                    .variables(variables)
                    .build();
            emailService.sendEmail(sitterEmail);

        } catch (Exception e) {
            log.error("Error al enviar notificación de cancelación para reserva {}: {}",
                    booking.getId(), e.getMessage());
        }
    }

    // ========== Notificaciones de Facturas (Invoice) ==========

    @Override
    @Async
    public void sendInvoiceEmail(Invoice invoice, byte[] pdfBytes) {
        log.info("Procesando envío de factura por email: {}", invoice.getInvoiceNumber());
        try {
            Map<String, Object> variables = createBaseInvoiceVariables(invoice);

            Attachment pdfAttachment = Attachment.builder()
                    .name("Factura-" + invoice.getInvoiceNumber() + ".pdf")
                    .contentType("application/pdf")
                    .resource(new ByteArrayResource(pdfBytes))
                    .build();

            Email email = Email.builder()
                    .to(invoice.getAccount().getOwnerUser().getEmail())
                    .subject("Tu factura de Petcare: " + invoice.getInvoiceNumber())
                    .templateName("invoice-notification")
                    .variables(variables)
                    .build();
            email.addAttachment(pdfAttachment);

            emailService.sendEmail(email);

        } catch (Exception e) {
            log.error("Error al enviar email de factura {}: {}", invoice.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void notifyInvoiceSent(Invoice invoice) {
        // Esta notificación podría ser para un admin o para el log.
        // Por ahora, solo registramos el evento.
        log.info("SIMULATING NOTIFICATION: Factura #{} enviada al cliente {}.",
                invoice.getInvoiceNumber(), invoice.getAccount().getOwnerUser().getEmail());
    }

    @Override
    @Async
    public void notifyInvoiceCancelled(Invoice invoice, String reason) {
        log.info("Procesando notificación de cancelación para factura: {}", invoice.getInvoiceNumber());
        try {
            Map<String, Object> variables = createBaseInvoiceVariables(invoice);
            variables.put("cancellationReason", reason);

            Email email = Email.builder()
                    .to(invoice.getAccount().getOwnerUser().getEmail())
                    .subject("Tu factura #" + invoice.getInvoiceNumber() + " ha sido cancelada")
                    .templateName("invoice-cancelled")
                    .variables(variables)
                    .build();

            emailService.sendEmail(email);

        } catch (Exception e) {
            log.error("Error al enviar email de cancelación de factura {}: {}", invoice.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void notifyInvoiceUpdated(Invoice invoice) {
        // Lógica similar para notificar actualizaciones de facturas si es necesario.
        log.info("SIMULATING NOTIFICATION: La factura #{} ha sido actualizada.", invoice.getInvoiceNumber());
    }

    @Override
    public void notifyInvoiceGenerated(Invoice savedInvoice) {

    }

    // ========== Métodos Privados Auxiliares ==========

    private void sendNewBookingNotificationToClient(Booking booking) {
        try {
            Map<String, Object> variables = createBaseBookingVariables(booking);
            Email email = Email.builder()
                    .to(booking.getBookedByUser().getEmail())
                    .subject("Confirmación de tu nueva reserva en Petcare #" + booking.getId())
                    .templateName("new-booking-client")
                    .variables(variables)
                    .build();
            emailService.sendEmail(email);
        } catch (Exception e) {
            log.error("Fallo al notificar al cliente sobre nueva reserva {}: {}", booking.getId(), e.getMessage());
        }
    }

    private void sendNewBookingNotificationToSitter(Booking booking) {
        try {
            Map<String, Object> variables = createBaseBookingVariables(booking);
            Email email = Email.builder()
                    .to(booking.getSitter().getEmail())
                    .subject("¡Nueva solicitud de reserva! - Petcare #" + booking.getId())
                    .templateName("new-booking-sitter")
                    .variables(variables)
                    .build();
            emailService.sendEmail(email);
        } catch (Exception e) {
            log.error("Fallo al notificar al cuidador sobre nueva reserva {}: {}", booking.getId(), e.getMessage());
        }
    }

    private Map<String, Object> createBaseBookingVariables(Booking booking) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("bookingId", booking.getId());
        variables.put("petName", booking.getPet().getName());
        variables.put("clientName", booking.getBookedByUser().getFullName());
        variables.put("sitterName", booking.getSitter().getFullName());
        variables.put("startDate", booking.getStartTime());
        variables.put("endDate", booking.getEndTime());
        variables.put("totalPrice", booking.getTotalPrice());
        variables.put("serviceName", booking.getServiceOffering().getServiceType());
        variables.put("notes", booking.getNotes());
        variables.put("scheduledDurationInMinutes", booking.getServiceOffering().getDurationInMinutes());
        variables.put("petId", booking.getPet().getId());
        variables.put("clientId", booking.getBookedByUser().getId());
        return variables;
    }

    private Map<String, Object> createBaseInvoiceVariables(Invoice invoice) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("invoiceNumber", invoice.getInvoiceNumber());
        variables.put("clientName", invoice.getAccount().getOwnerUser().getFullName());
        variables.put("issueDate", invoice.getIssueDate());
        variables.put("dueDate", invoice.getDueDate());
        variables.put("totalAmount", invoice.getTotalAmount());
        return variables;
    }
}