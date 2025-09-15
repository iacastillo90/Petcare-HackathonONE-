package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.SupportTicket.SupportTicketRequest;
import com.Petcare.Petcare.DTOs.SupportTicket.SupportTicketResponse;
import com.Petcare.Petcare.Services.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    // Crear un nuevo ticket
    @PostMapping
    public ResponseEntity<SupportTicketResponse> createTicket(@RequestBody SupportTicketRequest request) {
        SupportTicketResponse response = supportTicketService.create(request);
        return ResponseEntity.ok(response);
    }

    // Obtener ticket por ID
    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketResponse> getTicketById(@PathVariable Long id) {
        SupportTicketResponse response = supportTicketService.getById(id);
        return ResponseEntity.ok(response);
    }

    // Listar todos los tickets
    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> getAllTickets() {
        List<SupportTicketResponse> tickets = supportTicketService.getAll();
        return ResponseEntity.ok(tickets);
    }
}
