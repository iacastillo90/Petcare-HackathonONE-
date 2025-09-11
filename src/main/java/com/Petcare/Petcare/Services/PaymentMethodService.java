package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Payment.PaymentMethodMapper;
import com.Petcare.Petcare.DTOs.Payment.PaymentMethodRequest;
import com.Petcare.Petcare.DTOs.Payment.PaymentMethodResponse;
import com.Petcare.Petcare.DTOs.Payment.PaymentSummary;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Payment.PaymentMethod;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Repositories.PaymentMethodRepository;
import com.Petcare.Petcare.DTOs.Payment.PaymentMethodSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para manejar la lógica de negocio de métodos de pago.
 */
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final AccountRepository accountRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
                                AccountRepository accountRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.accountRepository = accountRepository;
    }

    public PaymentMethodResponse create(Long accountId, PaymentMethodRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + accountId));

        PaymentMethod entity = PaymentMethodMapper.toEntity(request, account);
        PaymentMethod saved = paymentMethodRepository.save(entity);

        return PaymentMethodMapper.toResponse(saved);
    }

    public List<PaymentMethodSummary> getAllByAccount(Long accountId) {
        return paymentMethodRepository.findByAccountId(accountId).stream()
                .map(PaymentMethodMapper::toSummary) // <-- Asegúrate de que el Mapper devuelva PaymentMethodSummary
                .collect(Collectors.toList());
    }

    public Optional<PaymentMethodResponse> getById(Long id) {
        return paymentMethodRepository.findById(id)
                .map(PaymentMethodMapper::toResponse);
    }

    public PaymentMethodResponse setDefault(Long id) {
        PaymentMethod entity = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado con ID: " + id));

        List<PaymentMethod> methods = paymentMethodRepository.findByAccountId(entity.getAccount().getId());
        methods.forEach(m -> {
            m.setDefault(false);
            paymentMethodRepository.save(m);
        });

        entity.setDefault(true);
        PaymentMethod updated = paymentMethodRepository.save(entity);

        return PaymentMethodMapper.toResponse(updated);
    }

    public void delete(Long id) {
        if (!paymentMethodRepository.existsById(id)) {
            throw new IllegalArgumentException("Método de pago no encontrado con ID: " + id);
        }
        paymentMethodRepository.deleteById(id);
    }
}
