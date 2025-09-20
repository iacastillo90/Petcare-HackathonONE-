package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.AppliedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppliedCouponRepository extends JpaRepository<AppliedCoupon, Long> {

    // Buscar por accountId (campo directo en la entidad)
    List<AppliedCoupon> findByAccountId(Long accountId);

    // Buscar por bookingId (campo directo en la entidad)
    List<AppliedCoupon> findByBookingId(Long bookingId);

    // Buscar por el id del cupón (relación ManyToOne). Usamos la notación underscore
    // para navegar a la propiedad id del objeto coupon.

    @Query("SELECT a FROM AppliedCoupon a WHERE a.coupon.id = :couponId")
    List<AppliedCoupon> findByCouponId(@Param("couponId") Long couponId);

    long countByCoupon_Id(Long couponId);
}
