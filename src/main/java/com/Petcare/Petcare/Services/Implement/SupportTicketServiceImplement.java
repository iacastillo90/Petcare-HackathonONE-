package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.SupportTicket.SupportTicketRequest;
import com.Petcare.Petcare.DTOs.SupportTicket.SupportTicketResponse;
import com.Petcare.Petcare.Models.SupportTicket.SupportTicket;
import com.Petcare.Petcare.Repositories.SupportTicketRepository;
import com.Petcare.Petcare.Services.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupportTicketServiceImplement implements SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Override
    public SupportTicketResponse create(SupportTicketRequest request) {
        SupportTicket ticket = new SupportTicket(
                request.getBookingId(),
                request.getReportId(),
                request.getAssignedAdminId(),
                request.getSubject(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority()
        );

        supportTicketRepository.save(ticket);
        return SupportTicketResponse.fromEntity(ticket);
    }

    @Override
    public SupportTicketResponse getById(Long id) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        return SupportTicketResponse.fromEntity(ticket);
    }

    @Override
    public List<SupportTicketResponse> getAll() {
        return supportTicketRepository.findAll()
                .stream()
                .map(SupportTicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SupportTicketResponse update(Long id, SupportTicketRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setBookingId(request.getBookingId());
        ticket.setReportId(request.getReportId());
        ticket.setAssignedAdminId(request.getAssignedAdminId());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(request.getStatus());
        ticket.setPriority(request.getPriority());

        supportTicketRepository.save(ticket);
        return SupportTicketResponse.fromEntity(ticket);
    }

    @Override
    public void delete(Long id) {
        if (!supportTicketRepository.existsById(id)) {
            throw new RuntimeException("Ticket no encontrado");
        }
        supportTicketRepository.deleteById(id);
    }
}
