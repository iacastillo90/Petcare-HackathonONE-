package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;
import com.Petcare.Petcare.Services.PlatformFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform-fees")
@RequiredArgsConstructor
public class PlatformFeeController {

    private final PlatformFeeService platformFeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo un administrador puede calcular tarifas
    public ResponseEntity<PlatformFeeResponse> createFee(
            @Valid @RequestBody CreatePlatformFeeRequest request
    ) {
        PlatformFeeResponse createdFee = platformFeeService.calculateAndCreateFee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFee);
    }
}