package com.Petcare.Petcare.DTOs.Payment;

import com.Petcare.Petcare.Models.Payment.PaymentMethod;
import com.Petcare.Petcare.Models.Account.Account;

public class PaymentMethodMapper {

    public static PaymentMethod toEntity(PaymentMethodRequest dto, Account account) {
        if (dto == null || account == null) return null;

        return new PaymentMethod(
                account,
                dto.getCardType(),
                dto.getLastFourDigits(),
                dto.getExpiryDate(),
                dto.getGatewayToken()
        );
    }

    public static PaymentMethodResponse toResponse(PaymentMethod entity) {
        if (entity == null) return null;

        return new PaymentMethodResponse(
                entity.getId(),
                entity.getAccount().getId(),
                entity.getCardType(),
                entity.getLastFourDigits(),
                entity.getExpiryDate(),
                entity.isDefault(),
                entity.isVerified(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PaymentMethodSummary toSummary(PaymentMethod entity) {
        if (entity == null) return null;

        return new PaymentMethodSummary(
                entity.getId(),
                entity.getCardType(),
                entity.getLastFourDigits(),
                entity.isDefault(),
                entity.isVerified(),
                entity.getCreatedAt()
        );
    }
}
