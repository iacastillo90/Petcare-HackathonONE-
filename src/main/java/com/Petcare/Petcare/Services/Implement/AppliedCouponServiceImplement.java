package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Exception.Business.*;
import com.Petcare.Petcare.Models.AppliedCoupon;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Repositories.AppliedCouponRepository;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.AppliedCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AppliedCouponServiceImplement implements AppliedCouponService {

    private final AppliedCouponRepository appliedCouponRepository;
    private final DiscountCouponRepository discountCouponRepository;
    private final BookingRepository bookingRepository;

    public AppliedCouponServiceImplement(AppliedCouponRepository appliedCouponRepository,
                                         DiscountCouponRepository discountCouponRepository,
                                         BookingRepository bookingRepository) {
        this.appliedCouponRepository = appliedCouponRepository;
        this.discountCouponRepository = discountCouponRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public AppliedCoupon applyCoupon(Long bookingId, Long accountId, String couponCode) {
        // 1️⃣ Buscar cupón
        Optional<DiscountCoupon> optionalCoupon = discountCouponRepository.findByCouponCode(couponCode);
        if (optionalCoupon.isEmpty()) {
            throw new CouponNotFoundException(couponCode);
        }
        DiscountCoupon coupon = optionalCoupon.get();

        // 2️⃣ Validar cupón
        if (!validateCoupon(coupon)) {
            throw new CouponExpiredException(couponCode);
        }

        // 3️⃣ Calcular descuento
        BigDecimal discountAmount;
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (coupon.getDiscountType().name().equals("PERCENTAGE")) {
            discountAmount = booking.getTotalPrice()
                    .multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        // 4️⃣ Limitar descuento al total de la reserva
        if (discountAmount.compareTo(booking.getTotalPrice()) > 0) {
            discountAmount = booking.getTotalPrice();
        }

        // 5️⃣ Actualizar total de la reserva
        booking.setTotalPrice(booking.getTotalPrice().subtract(discountAmount));
        bookingRepository.save(booking);

        // 6️⃣ Crear AppliedCoupon
        AppliedCoupon appliedCoupon = new AppliedCoupon();
        appliedCoupon.setBookingId(bookingId);
        appliedCoupon.setAccountId(accountId);
        appliedCoupon.setCoupon(coupon);
        appliedCoupon.setDiscountAmount(discountAmount);
        appliedCoupon.setAppliedAt(LocalDateTime.now());

        // 7️⃣ Guardar AppliedCoupon
        AppliedCoupon saved = appliedCouponRepository.save(appliedCoupon);



        return saved;
    }

    @Override
    public List<AppliedCoupon> getCouponsByAccount(Long accountId) {
        return appliedCouponRepository.findByAccountId(accountId);
    }

    @Override
    public List<AppliedCoupon> getCouponsByBooking(Long bookingId) {
        return appliedCouponRepository.findByBookingId(bookingId);
    }

    @Override
    public List<AppliedCoupon> getCouponsByCoupon(Long couponId) {
        return appliedCouponRepository.findByCouponId(couponId);
    }

    @Override
    public boolean validateCoupon(DiscountCoupon coupon) {
        if (!coupon.isActive()) return false;
        return coupon.getExpiryDate().isAfter(LocalDateTime.now());
    }

    // ========== MÉTODOS ASYNC ==========

    @Async("taskExecutor")
    public CompletableFuture<List<AppliedCoupon>> getCouponsByAccountAsync(Long accountId) {
        log.debug("Executing getCouponsByAccountAsync({}) in background thread", accountId);
        List<AppliedCoupon> coupons = getCouponsByAccount(accountId);
        return CompletableFuture.completedFuture(coupons);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<AppliedCoupon>> getCouponsByBookingAsync(Long bookingId) {
        log.debug("Executing getCouponsByBookingAsync({}) in background thread", bookingId);
        List<AppliedCoupon> coupons = getCouponsByBooking(bookingId);
        return CompletableFuture.completedFuture(coupons);
    }
}
