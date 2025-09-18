package com.Petcare.Petcare.DTOs.PlatformFee;

import com.Petcare.Petcare.Models.PlatformFee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestPlatformFeeResponse {
    private BigDecimal percentage;

    public static LatestPlatformFeeResponse fromEntity(PlatformFee platformFee) {
        return new LatestPlatformFeeResponse(platformFee.getFeePercentage());
    }
}