package com.Petcare.Petcare.DTOs.Account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    private Long id;
    private String accountNumber;
    private String ownerName;
    private String email;
}
