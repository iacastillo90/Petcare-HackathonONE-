package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.DiscountCouponService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
