package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.LatestPlatformFeeResponse;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;
import com.Petcare.Petcare.Models.PlatformFee;
import com.Petcare.Petcare.Services.PlatformFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform-fees")
@RequiredArgsConstructor
public class PlatformFeeController {

    @Autowired
    private PlatformFeeService platformFeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo un administrador puede calcular tarifas
    public ResponseEntity<PlatformFeeResponse> createFee(
            @Valid @RequestBody CreatePlatformFeeRequest request
    ) {
        PlatformFeeResponse createdFee = platformFeeService.calculateAndCreateFee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFee);
    }

    /**
     * Expone un endpoint público para que cualquiera (especialmente el frontend)
     * pueda consultar la tarifa de servicio activa más reciente.
     */
    @GetMapping("/latest")
    public ResponseEntity<LatestPlatformFeeResponse> getLatestActiveFee() {
        PlatformFee latestFee = platformFeeService.getLatestActiveFee();
        LatestPlatformFeeResponse response = LatestPlatformFeeResponse.fromEntity(latestFee);
        return ResponseEntity.ok(response);
    }
}