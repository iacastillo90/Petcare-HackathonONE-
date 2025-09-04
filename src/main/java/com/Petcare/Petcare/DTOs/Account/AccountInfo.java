package com.Petcare.Petcare.DTOs.Account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Información resumida de la cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    private Long id;
    private String accountNumber;
    private String ownerName;
    private String email;
}
