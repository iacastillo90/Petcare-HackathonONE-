package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad PaymentMethod.
 *
 * Proporciona métodos de acceso a datos para manejar los métodos de pago
 * registrados por los usuarios en el sistema.
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Encuentra todos los métodos de pago asociados a una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return lista de métodos de pago asociados
     */
    List<PaymentMethod> findByAccountId(Long accountId);

    /**
     * Encuentra el método de pago por su token único de pasarela.
     *
     * @param gatewayToken token de la pasarela
     * @return método de pago (si existe)
     */
    Optional<PaymentMethod> findByGatewayToken(String gatewayToken);

    /**
     * Encuentra el método de pago predeterminado de una cuenta.
     *
     * @param accountId ID de la cuenta
     * @return método de pago predeterminado (si existe)
     */
    Optional<PaymentMethod> findByAccountIdAndIsDefaultTrue(Long accountId);
}
