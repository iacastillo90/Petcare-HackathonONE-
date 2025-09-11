package com.Petcare.Petcare.DTOs.Payment;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PaymentMethodSummary implements Serializable {

    private Long id;
    private String cardType;
    private String lastFourDigits;
    private boolean isDefault;
    private boolean isVerified;
    private LocalDateTime createdAt;

    public PaymentMethodSummary() {}

    public PaymentMethodSummary(Long id, String cardType, String lastFourDigits,
                                boolean isDefault, boolean isVerified, LocalDateTime createdAt) {
        this.id = id;
        this.cardType = cardType;
        this.lastFourDigits = lastFourDigits;
        this.isDefault = isDefault;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
    }




// Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getLastFourDigits() { return lastFourDigits; }
    public void setLastFourDigits(String lastFourDigits) { this.lastFourDigits = lastFourDigits; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
