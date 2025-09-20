package com.Petcare.Petcare.DTOs.Account;

import java.math.BigDecimal;

/**
 * DTO resumido de una cuenta.
 * Usado en listados rápidos y dashboards donde no se requiere toda la información detallada.
 */
public class AccountSummary {

    private Long id;
    private String accountName;
    private String accountNumber;
    private BigDecimal balance;
    private boolean isActive;

    public AccountSummary() {}

    public AccountSummary(Long id, String accountName, String accountNumber,
                          BigDecimal balance, boolean isActive) {
        this.id = id;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.isActive = isActive;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
