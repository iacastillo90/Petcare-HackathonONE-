package com.Petcare.Petcare.Models.SupportTicket;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un ticket de soporte.
 */
@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long reportId;
    private Long assignedAdminId; // Admin asignado

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTOR VACÍO =====
    public SupportTicket(Long bookingId, Long reportId, Long assignedAdminId, String subject, String description, TicketStatus status, TicketPriority priority) {
    }

    // ===== CONSTRUCTOR CON CAMPOS PRINCIPALES =====
    public SupportTicket(String subject, String description, TicketStatus status, TicketPriority priority) {
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== GETTERS Y SETTERS =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getAssignedAdminId() { return assignedAdminId; }
    public void setAssignedAdminId(Long assignedAdminId) { this.assignedAdminId = assignedAdminId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
