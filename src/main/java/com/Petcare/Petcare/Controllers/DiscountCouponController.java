package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Services.DiscountCouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/discount-coupons")
public class DiscountCouponController {

    private final DiscountCouponService discountCouponService;

    public DiscountCouponController(DiscountCouponService discountCouponService) {
        this.discountCouponService = discountCouponService;
    }

    // Crear cupón
    @PostMapping
    public ResponseEntity<DiscountCoupon> createCoupon(@RequestBody DiscountCoupon discountCoupon) {
        return ResponseEntity.ok(discountCouponService.saveDiscountCoupon(discountCoupon));
    }

    // Obtener cupón por ID
    @GetMapping("/{id}")
    public ResponseEntity<DiscountCoupon> getCouponById(@PathVariable Long id) {
        Optional<DiscountCoupon> coupon = discountCouponService.getDiscountCouponById(id);
        return coupon.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Obtener cupón por código
    @GetMapping("/code/{couponCode}")
    public ResponseEntity<DiscountCoupon> getCouponByCode(@PathVariable String couponCode) {
        Optional<DiscountCoupon> coupon = discountCouponService.getDiscountCouponByCode(couponCode);
        return coupon.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Listar todos los cupones
    @GetMapping
    public ResponseEntity<List<DiscountCoupon>> getAllCoupons() {
        return ResponseEntity.ok(discountCouponService.getAllDiscountCoupons());
    }

    // Eliminar cupón
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        discountCouponService.deleteDiscountCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
