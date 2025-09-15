package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SupportTicket.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
}
