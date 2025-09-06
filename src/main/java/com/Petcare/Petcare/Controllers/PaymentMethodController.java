package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Payment.PaymentMethodRequest;
import com.Petcare.Petcare.DTOs.Payment.PaymentMethodResponse;
import com.Petcare.Petcare.DTOs.Payment.PaymentMethodSummary;
import com.Petcare.Petcare.Services.PaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de métodos de pago.
 */
@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Crear un nuevo método de pago para una cuenta.
     */
    @PostMapping("/{accountId}")
    public ResponseEntity<PaymentMethodResponse> create(
            @PathVariable Long accountId,
            @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.create(accountId, request));
    }

    /**
     * Listar todos los métodos de pago de una cuenta.
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<PaymentMethodSummary>> getAllByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(paymentMethodService.getAllByAccount(accountId));
    }


    /**
     * Obtener un método de pago por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> getById(@PathVariable Long id) {
        Optional<PaymentMethodResponse> response = paymentMethodService.getById(id);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Establecer un método de pago como predeterminado.
     */
    @PutMapping("/{id}/default")
    public ResponseEntity<PaymentMethodResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.setDefault(id));
    }

    /**
     * Eliminar un método de pago por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
