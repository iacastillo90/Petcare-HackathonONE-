package com.Petcare.Petcare.DTOs.Account;

import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar la creación de una nueva cuenta de cliente.
 * <p>
 * Este objeto contiene los datos mínimos y necesarios que un cliente debe proporcionar
 * para registrar una nueva cuenta en el sistema.
 *
 * @param ownerUserId   El identificador único del usuario que será el propietario de la cuenta. Es un campo obligatorio.
 * @param accountName   Un nombre descriptivo y amigable para la cuenta (ej: "Familia Pérez"). Es obligatorio y tiene un límite de 100 caracteres.
 * @param currency      El código de moneda de 3 letras (ISO 4217) para la cuenta (ej: "USD", "CLP"). Si no se proporciona, el sistema puede asignar un valor por defecto.
 */
public class CreateAccountRequest{

    @NotNull(message = "El ID del usuario propietario es obligatorio.")
    private Long ownerUserId;

    @NotNull(message = "El usuario propietario es obligatorio")
    private User ownerUser;

    @NotBlank(message = "El nombre de la cuenta es obligatorio.")
    @Size(max = 100, message = "El nombre de la cuenta no puede exceder los 100 caracteres.")
    private String accountName;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 50, message = "El número de cuenta no puede exceder 50 caracteres")
    private String accountNumber;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(Account account){
        ownerUserId = account.getId();
        ownerUser = account.getOwnerUser();
        accountName = account.getAccountName();
        accountNumber = account.getAccountNumber();

    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
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


}