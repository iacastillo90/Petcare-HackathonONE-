package com.Petcare.Petcare.DTOs.Payment;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PaymentMethodResponse implements Serializable {

    private Long id;
    private Long accountId;
    private String cardType;
    private String lastFourDigits;
    private String expiryDate;
    private boolean isDefault;
    private boolean isVerified;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentMethodResponse() {}

    public PaymentMethodResponse(Long id, Long accountId, String cardType, String lastFourDigits,
                                 String expiryDate, boolean isDefault, boolean isVerified,
                                 boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.cardType = cardType;
        this.lastFourDigits = lastFourDigits;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault;
        this.isVerified = isVerified;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }




// Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getLastFourDigits() { return lastFourDigits; }
    public void setLastFourDigits(String lastFourDigits) { this.lastFourDigits = lastFourDigits; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
