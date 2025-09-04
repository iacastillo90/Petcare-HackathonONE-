package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Invoice.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findById(Long invoiceId);

    Page<Invoice> findByAccountId(Long accountId, Pageable pageable);

    boolean existsByBookingId(Long id);
}
