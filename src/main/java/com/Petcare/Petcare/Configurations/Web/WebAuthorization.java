package com.Petcare.Petcare.Configurations.Web;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase de configuración de Spring Security para definir políticas de autorización y configurar la cadena de filtros de seguridad.
 * Maneja la autenticación basada en JWT y asegura que las sesiones sean sin estado.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebAuthorization {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    private static final Logger logger = LoggerFactory.getLogger(WebAuthorization.class);

    /**
     * Define la cadena de filtros de seguridad para configurar las políticas de seguridad de la aplicación.
     *
     * @param http Objeto HttpSecurity para configurar la seguridad.
     * @return Un objeto SecurityFilterChain con las reglas de seguridad.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain");
        return http
                .csrf(csrf ->
                        csrf.disable())
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users/register-sitter").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/verify").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/email-available").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/health").permitAll()
                                .requestMatchers(HttpMethod.GET, "/verification-success.html").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/summary").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api-docs", "/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                //.requestMatchers("/api/**").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManager ->
                        sessionManager
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}