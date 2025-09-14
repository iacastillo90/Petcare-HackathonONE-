package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.DiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    // Buscar un cupón por su código único
    Optional<DiscountCoupon> findByCouponCode(String couponCode);

    // Verificar si un código de cupón ya existe (para evitar duplicados)
    boolean existsByCouponCode(String couponCode);
}
