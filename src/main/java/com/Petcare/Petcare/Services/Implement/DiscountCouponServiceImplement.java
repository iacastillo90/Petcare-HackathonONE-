package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.DiscountCouponService;
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
    public DiscountCoupon saveDiscountCoupon(DiscountCoupon discountCoupon) {
        return discountCouponRepository.save(discountCoupon);
    }

    @Override
    public Optional<DiscountCoupon> getDiscountCouponById(Long id) {
        return discountCouponRepository.findById(id);
    }

    @Override
    public Optional<DiscountCoupon> getDiscountCouponByCode(String couponCode) {
        return discountCouponRepository.findByCouponCode(couponCode);
    }

    @Override
    public List<DiscountCoupon> getAllDiscountCoupons() {
        return discountCouponRepository.findAll();
    }

    @Override
    public void deleteDiscountCoupon(Long id) {
        discountCouponRepository.deleteById(id);
    }
}
