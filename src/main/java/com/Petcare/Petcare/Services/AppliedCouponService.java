package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.Models.AppliedCoupon;
import com.Petcare.Petcare.Models.DiscountCoupon;

import java.util.List;

/**
 * Servicio para la gestión de cupones aplicados.
 */
public interface AppliedCouponService {

    /**
     * Aplica un cupón de descuento a una reserva específica.
     *
     * @param bookingId ID de la reserva
     * @param accountId ID de la cuenta del usuario
     * @param couponCode Código del cupón
     * @return AppliedCoupon registro del cupón aplicado
     */
    AppliedCoupon applyCoupon(Long bookingId, Long accountId, String couponCode);

    /**
     * Obtiene todos los cupones aplicados por un usuario.
     *
     * @param accountId ID de la cuenta
     * @return lista de AppliedCoupon
     */
    List<AppliedCoupon> getCouponsByAccount(Long accountId);

    /**
     * Obtiene todos los cupones aplicados en una reserva.
     *
     * @param bookingId ID de la reserva
     * @return lista de AppliedCoupon
     */
    List<AppliedCoupon> getCouponsByBooking(Long bookingId);

    /**
     * Obtiene todas las aplicaciones de un cupón específico.
     *
     * @param couponId ID del cupón
     * @return lista de AppliedCoupon
     */
    List<AppliedCoupon> getCouponsByCoupon(Long couponId);

    /**
     * Valida si un cupón es aplicable antes de usarlo.
     *
     * @param coupon Código del cupón
     * @return true si es válido, false si no
     */
    boolean validateCoupon(DiscountCoupon coupon);
}
