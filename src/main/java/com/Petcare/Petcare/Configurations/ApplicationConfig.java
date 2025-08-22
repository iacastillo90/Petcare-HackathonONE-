package com.Petcare.Petcare.Configurations;

import com.Petcare.Petcare.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationConfig.class);

    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define un bean de tipo AuthenticationManager para ser usado por Spring Security.
     *
     * @param config La configuración de autenticación proporcionada por Spring.
     * @return Un objeto AuthenticationManager para manejar la autenticación.
     * @throws RuntimeException Si ocurre un error al crear el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            logger.info("Creating AuthenticationManager bean");
            return config.getAuthenticationManager();
        } catch (Exception e) {
            logger.error("Error creating AuthenticationManager bean: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Define un bean de tipo UserDetailsService para cargar los detalles de un usuario por su nombre de usuario.
     *
     * @return Un objeto UserDetailsService que busca usuarios en el repositorio.
     * @throws UsernameNotFoundException Si el usuario no se encuentra.
     */
    @Bean
    public UserDetailsService userDetailService() {
        return email -> {
            logger.info("Attempting to load user: {}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("User not found: {}", email);
                        return new UsernameNotFoundException("User not found");
                    });
        };
    }

    /**
     * Define un bean de tipo AuthenticationProvider para manejar la autenticación.
     *
     * @param userDetailsService El servicio de detalles de usuario
     * @param passwordEncoder El encoder de contraseñas
     * @return Un objeto AuthenticationProvider configurado con UserDetailsService y PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

}
