package com.Petcare.Petcare.DTOs.SupportTicket;

public class CreateSupportTicketRequest {
    private Long bookingId;
    private Long reportId;
    private Long assignedAdminId;
    private String subject;
    private String description;

    // Getters y Setters
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
}
