package com.Petcare.Petcare.Configurations.Auth;

import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Request.RegisterRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la gestión de autenticación de usuarios.
 * Proporciona endpoints para el inicio de sesión y el registro de usuarios.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Autentica a un usuario y genera un token JWT.
     *
     * @param request Objeto que contiene las credenciales del usuario (nombre de usuario y contraseña).
     * @return ResponseEntity con el token JWT en caso de éxito o un error en caso de fallo.
     */
    @PostMapping("/login")
    @Operation(summary = "Log in a user", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        logger.info("Received login request for username: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        logger.info("Login response sent for username: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un nuevo usuario en el sistema y genera un token JWT.
     *
     * @param request Objeto que contiene los datos del usuario para el registro (nombre de usuario, contraseña, email, etc.).
     * @return ResponseEntity con el token JWT en caso de éxito o un error en caso de fallo.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate username/email")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Received registration request for username: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        logger.info("Registration response sent for username: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}