package com.Petcare.Petcare.DTOs.SupportTicket;

import com.Petcare.Petcare.Models.SupportTicket.SupportTicket;
import com.Petcare.Petcare.Models.SupportTicket.TicketPriority;
import com.Petcare.Petcare.Models.SupportTicket.TicketStatus;

public class SupportTicketSummary {
    private Long id;
    private String subject;
    private TicketStatus status;
    private TicketPriority priority;

    public SupportTicketSummary() {}

    public SupportTicketSummary(Long id, String subject, TicketStatus status, TicketPriority priority) {
        this.id = id;
        this.subject = subject;
        this.status = status;
        this.priority = priority;
    }

    public static SupportTicketSummary fromEntity(SupportTicket ticket) {
        return new SupportTicketSummary(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getStatus(),
                ticket.getPriority()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
}
