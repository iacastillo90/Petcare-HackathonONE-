package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.DiscountCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DiscountCouponServiceImplement implements DiscountCouponService {

    private final DiscountCouponRepository discountCouponRepository;

    public DiscountCouponServiceImplement(DiscountCouponRepository discountCouponRepository) {
        this.discountCouponRepository = discountCouponRepository;
    }

    @Override
    @CacheEvict(value = "discounts", allEntries = true)
    public DiscountCoupon saveDiscountCoupon(DiscountCoupon discountCoupon) {
        return discountCouponRepository.save(discountCoupon);
    }

    @Override
    public Optional<DiscountCoupon> getDiscountCouponById(Long id) {
        return discountCouponRepository.findById(id);
    }

    @Override
    @Cacheable(value = "discounts", key = "#couponCode")
    public Optional<DiscountCoupon> getDiscountCouponByCode(String couponCode) {
        return discountCouponRepository.findByCouponCode(couponCode);
    }

    @Override
    @Cacheable(value = "discounts", key = "'all'")
    public List<DiscountCoupon> getAllDiscountCoupons() {
        return discountCouponRepository.findAll();
    }

    @Override
    @CacheEvict(value = "discounts", key = "#id")
    public void deleteDiscountCoupon(Long id) {
        discountCouponRepository.deleteById(id);
    }

    // ========== MÉTODOS ASYNC ==========

    @Async("taskExecutor")
    public CompletableFuture<List<DiscountCoupon>> getAllDiscountCouponsAsync() {
        log.debug("Executing getAllDiscountCouponsAsync in background thread");
        List<DiscountCoupon> coupons = getAllDiscountCoupons();
        return CompletableFuture.completedFuture(coupons);
    }

    @Async("taskExecutor")
    public CompletableFuture<Optional<DiscountCoupon>> getDiscountCouponByCodeAsync(String couponCode) {
        log.debug("Executing getDiscountCouponByCodeAsync({}) in background thread", couponCode);
        Optional<DiscountCoupon> coupon = getDiscountCouponByCode(couponCode);
        return CompletableFuture.completedFuture(coupon);
    }
}
