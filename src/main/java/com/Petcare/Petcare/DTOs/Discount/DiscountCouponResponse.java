package com.Petcare.Petcare.DTOs.Discount;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Models.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para devolver la información de un cupón de descuento.
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see DiscountCoupon
 */
@Schema(description = "DTO con la información completa de un cupón de descuento.")
public record DiscountCouponResponse(
        @Schema(description = "Identificador único del cupón.", example = "1")
        Long id,

        @Schema(description = "Código único del cupón para aplicar en reservas.", example = "DESCUENTO20")
        String couponCode,

        @Schema(description = "Tipo de descuento (PORCENTAJE o MONTO_FIJO).", example = "PORCENTAJE")
        DiscountType discountType,

        @Schema(description = "Valor del descuento (porcentaje o monto fijo).", example = "20.00")
        BigDecimal discountValue,

        @Schema(description = "Fecha de expiración del cupón.", example = "2025-12-31T23:59:59")
        LocalDateTime expiryDate,

        @Schema(description = "Cantidad máxima de usos permitidos.", example = "100")
        Integer maxUses,

        @Schema(description = "Indica si el cupón está activo.", example = "true")
        boolean active,

        @Schema(description = "Cantidad de veces que el cupón ha sido usado.", example = "25")
        Integer usedCount
) {

    /**
     * Convierte una entidad del modelo {@link DiscountCoupon} a un DTO {@link DiscountCouponResponse}.
     *
     * @param coupon la entidad DiscountCoupon a convertir. Puede ser null.
     * @return nueva instancia de DiscountCouponResponse con datos poblados, o null si el parámetro es null.
     */
    public static DiscountCouponResponse fromEntity(DiscountCoupon coupon) {
        if (coupon == null) {
            return null;
        }
        return new DiscountCouponResponse(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getExpiryDate(),
                coupon.getMaxUses(),
                coupon.isActive(),
                coupon.getUsedCount()
        );
    }

    /**
     * Verifica si el cupón está activo.
     *
     * @return true si el cupón está activo.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Calcula los usos restantes del cupón.
     *
     * @return número de usos restantes, o 0 si ya se agotó.
     */
    public int getRemainingUses() {
        if (maxUses == null || usedCount == null) {
            return 0;
        }
        return Math.max(0, maxUses - usedCount);
    }

    /**
     * Verifica si el cupón ha expirado.
     *
     * @return true si la fecha de expiración es anterior a ahora.
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
