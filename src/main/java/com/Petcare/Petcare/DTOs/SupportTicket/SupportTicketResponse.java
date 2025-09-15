package com.Petcare.Petcare.DTOs.SupportTicket;

import com.Petcare.Petcare.Models.SupportTicket.SupportTicket;
import com.Petcare.Petcare.Models.SupportTicket.TicketPriority;
import com.Petcare.Petcare.Models.SupportTicket.TicketStatus;

import java.time.LocalDateTime;

public class SupportTicketResponse {
    private Long id;
    private Long bookingId;
    private Long reportId;
    private Long assignedAdminId;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor vacío
    public SupportTicketResponse() {}

    // Constructor completo
    public SupportTicketResponse(Long id, Long bookingId, Long reportId, Long assignedAdminId,
                                 String subject, String description, TicketStatus status,
                                 TicketPriority priority, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.reportId = reportId;
        this.assignedAdminId = assignedAdminId;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ================= Métodos de conversión =================
    public static SupportTicketResponse fromEntity(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getBookingId(),
                ticket.getReportId(),
                ticket.getAssignedAdminId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Long assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
