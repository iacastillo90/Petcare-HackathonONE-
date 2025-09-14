package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.Models.DiscountCoupon;

import java.util.List;
import java.util.Optional;

public interface DiscountCouponService {

    // Crear o actualizar un cupón
    DiscountCoupon saveDiscountCoupon(DiscountCoupon discountCoupon);

    // Buscar un cupón por ID
    Optional<DiscountCoupon> getDiscountCouponById(Long id);

    // Buscar un cupón por código
    Optional<DiscountCoupon> getDiscountCouponByCode(String couponCode);

    // Listar todos los cupones
    List<DiscountCoupon> getAllDiscountCoupons();

    // Eliminar un cupón
    void deleteDiscountCoupon(Long id);
}
