package com.Petcare.Petcare.DTOs.SupportTicket;

import com.Petcare.Petcare.Models.SupportTicket.TicketStatus;
import com.Petcare.Petcare.Models.SupportTicket.TicketPriority;

public class SupportTicketRequest {

    private Long bookingId;
    private Long reportId;
    private Long assignedAdminId;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;

    public SupportTicketRequest() {}

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
}
