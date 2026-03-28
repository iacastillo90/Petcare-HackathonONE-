package com.Petcare.Petcare.DTOs.Discount;

import com.Petcare.Petcare.Models.AppliedCoupon;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar la respuesta cuando un cupón ha sido aplicado exitosamente.
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see AppliedCoupon
 */
@Schema(description = "DTO con la información de un cupón aplicado a una reserva.")
public record AppliedCouponResponse(
        @Schema(description = "Identificador único del registro de aplicación.", example = "1")
        Long id,

        @Schema(description = "ID de la reserva donde se aplicó el cupón.", example = "5")
        Long bookingId,

        @Schema(description = "ID de la cuenta donde se aplicó el cupón.", example = "3")
        Long accountId,

        @Schema(description = "ID del cupón aplicado.", example = "10")
        Long couponId,

        @Schema(description = "Monto del descuento aplicado.", example = "15.00")
        BigDecimal discountAmount,

        @Schema(description = "Total actualizado de la reserva después del descuento.", example = "85.00")
        BigDecimal bookingTotal,

        @Schema(description = "Fecha y hora cuando se aplicó el cupón.", example = "2025-02-15T10:30:00")
        LocalDateTime appliedAt
) {

    /**
     * Constructor para crear DTO desde entidad AppliedCoupon.
     *
     * @param appliedCoupon la entidad AppliedCoupon a convertir.
     * @param bookingTotal el total actualizado de la reserva después del descuento.
     */
    public AppliedCouponResponse(AppliedCoupon appliedCoupon, BigDecimal bookingTotal) {
        this(
                appliedCoupon.getId(),
                appliedCoupon.getBookingId(),
                appliedCoupon.getAccountId(),
                appliedCoupon.getCouponId(),
                appliedCoupon.getDiscountAmount(),
                bookingTotal,
                appliedCoupon.getAppliedAt()
        );
    }

    /**
     * Convierte una entidad del modelo {@link AppliedCoupon} a un DTO {@link AppliedCouponResponse}.
     *
     * @param appliedCoupon la entidad AppliedCoupon a convertir. Puede ser null.
     * @param bookingTotal el total actualizado de la reserva después del descuento.
     * @return nueva instancia de AppliedCouponResponse con datos poblados, o null si el parámetro es null.
     */
    public static AppliedCouponResponse fromEntity(AppliedCoupon appliedCoupon, BigDecimal bookingTotal) {
        if (appliedCoupon == null) {
            return null;
        }
        return new AppliedCouponResponse(appliedCoupon, bookingTotal);
    }

    /**
     * Convierte una entidad del modelo {@link AppliedCoupon} a un DTO {@link AppliedCouponResponse}
     * sin el total de la reserva.
     *
     * @param appliedCoupon la entidad AppliedCoupon a convertir. Puede ser null.
     * @return nueva instancia de AppliedCouponResponse con datos poblados, o null si el parámetro es null.
     */
    public static AppliedCouponResponse fromEntity(AppliedCoupon appliedCoupon) {
        return fromEntity(appliedCoupon, null);
    }
}
