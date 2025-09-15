package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.SupportTicket.SupportTicketRequest;
import com.Petcare.Petcare.DTOs.SupportTicket.SupportTicketResponse;

import java.util.List;

public interface SupportTicketService {

    SupportTicketResponse create(SupportTicketRequest request);

    SupportTicketResponse getById(Long id);

    List<SupportTicketResponse> getAll();

    SupportTicketResponse update(Long id, SupportTicketRequest request);

    void delete(Long id);
}
