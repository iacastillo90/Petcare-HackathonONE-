package com.Petcare.Petcare.Configurations.Auth;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.Models.Auth.Request.LoginRequest;
import com.Petcare.Petcare.Models.Auth.Request.RegisterRequest;
import com.Petcare.Petcare.Models.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de autenticación y registro de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Autentica a un usuario y genera un token JWT.
     *
     * @param request Contiene email y password.
     * @return AuthResponse con el token JWT.
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication, which should not happen."));

        String token = jwtService.getToken(user);
        logger.info("Login successful for email: {}", request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }

    /**
     * Registra un nuevo usuario en el sistema y genera un token JWT.
     *
     * @param request Contiene los datos para el nuevo usuario.
     * @return AuthResponse con el token JWT.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} already exists.", request.getEmail());
            throw new IllegalArgumentException("Email already in use.");
        }

        User newUser = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .isActive(true)
                .build();

        userRepository.save(newUser);
        logger.info("User registered successfully with email: {}", request.getEmail());

        String token = jwtService.getToken(newUser);

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}