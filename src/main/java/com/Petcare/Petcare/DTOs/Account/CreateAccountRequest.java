package com.Petcare.Petcare.DTOs.Account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación de una cuenta.
 */
public class CreateAccountRequest {

    @NotBlank(message = "El nombre de la cuenta es obligatorio")
    @Size(max = 100, message = "El nombre de la cuenta no puede exceder los 100 caracteres")
    private String accountName;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 50, message = "El número de cuenta no puede exceder los 50 caracteres")
    private String accountNumber;

    private String currency = "USD"; // Valor por defecto

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(String accountName, String accountNumber, String currency) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.currency = currency;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
