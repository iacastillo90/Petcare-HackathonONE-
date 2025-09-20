package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Discount.AppliedCouponResponse;
import com.Petcare.Petcare.Models.AppliedCoupon;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Services.AppliedCouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para la gestión de cupones aplicados (AppliedCoupon).
 *
 * <p>Permite aplicar cupones a reservas existentes y consultar
 * los cupones aplicados por cuenta, reserva o cupón específico.</p>
 *
 * <p>Este controller no maneja facturación directa; la integración
 * con Invoice se realiza en otro módulo gestionado por el equipo correspondiente.</p>
 *
 * <p>Endpoints disponibles:</p>
 * <ul>
 *   <li>POST /api/applied-coupons/apply → Aplica un cupón a una reserva</li>
 *   <li>GET /api/applied-coupons/by-account/{accountId} → Lista cupones aplicados por cuenta</li>
 *   <li>GET /api/applied-coupons/by-booking/{bookingId} → Lista cupones aplicados a una reserva</li>
 *   <li>GET /api/applied-coupons/by-coupon/{couponId} → Lista todas las aplicaciones de un cupón</li>
 * </ul>
 *
 * <p>Autor: Equipo Petcare 10</p>
 * @version 1.0
 */
@RestController
@RequestMapping("/api/applied-coupons")
public class AppliedCouponController {

    private final AppliedCouponService appliedCouponService;
    private final BookingRepository bookingRepository;

    public AppliedCouponController(AppliedCouponService appliedCouponService,
                                   BookingRepository bookingRepository) {
        this.appliedCouponService = appliedCouponService;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Aplica un cupón de descuento a una reserva específica.
     *
     * <p>Calcula el descuento, lo registra como AppliedCoupon y devuelve
     * el total actualizado de la reserva.</p>
     *
     * @param bookingId ID de la reserva
     * @param accountId ID de la cuenta del usuario
     * @param couponCode Código del cupón a aplicar
     * @return ResponseEntity con los detalles del cupón aplicado
     */
    @PostMapping("/apply")
    public ResponseEntity<AppliedCouponResponse> applyCoupon(@RequestParam Long bookingId,
                                                             @RequestParam Long accountId,
                                                             @RequestParam String couponCode) {
        AppliedCoupon appliedCoupon = appliedCouponService.applyCoupon(bookingId, accountId, couponCode);

        AppliedCouponResponse response = new AppliedCouponResponse();
        response.setId(appliedCoupon.getId());
        response.setBookingId(appliedCoupon.getBookingId());
        response.setAccountId(appliedCoupon.getAccountId());
        response.setCouponId(appliedCoupon.getCouponId());
        response.setDiscountAmount(appliedCoupon.getDiscountAmount());
        response.setAppliedAt(appliedCoupon.getAppliedAt());

        // Obtener total actualizado de la reserva
        Booking booking = bookingRepository.findById(appliedCoupon.getBookingId()).orElse(null);
        if (booking != null) {
            response.setBookingTotal(booking.getTotalPrice());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los cupones aplicados por una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return ResponseEntity con la lista de AppliedCoupon
     */
    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<?> getByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(appliedCouponService.getCouponsByAccount(accountId));
    }

    /**
     * Obtiene todos los cupones aplicados a una reserva específica.
     *
     * @param bookingId ID de la reserva
     * @return ResponseEntity con la lista de AppliedCoupon
     */
    @GetMapping("/by-booking/{bookingId}")
    public ResponseEntity<?> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(appliedCouponService.getCouponsByBooking(bookingId));
    }

    /**
     * Obtiene todas las aplicaciones de un cupón específico.
     *
     * @param couponId ID del cupón
     * @return ResponseEntity con la lista de AppliedCoupon
     */
    @GetMapping("/by-coupon/{couponId}")
    public ResponseEntity<?> getByCoupon(@PathVariable Long couponId) {
        return ResponseEntity.ok(appliedCouponService.getCouponsByCoupon(couponId));
    }
}
