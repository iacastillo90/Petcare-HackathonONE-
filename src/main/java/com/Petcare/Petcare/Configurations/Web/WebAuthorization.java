package com.Petcare.Petcare.Configurations.Web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtAuthenticationFilter;

/**
 * Configuración de autorización web y cadena de filtros de seguridad.
 * Maneja autenticación JWT sin estado, CORS integrado con Spring Security,
 * y define reglas de acceso público/protegido para endpoints de la API.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebAuthorization {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Define la cadena de filtros de seguridad para configurar las políticas de seguridad de la aplicación.
     *
     * @param http Objeto HttpSecurity para configurar la seguridad.
     * @return Un objeto SecurityFilterChain con las reglas de seguridad.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf ->
                        csrf.disable())
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users/register-sitter").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/verify").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/email-available").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/health").permitAll()
                                .requestMatchers(HttpMethod.GET, "/verification-success.html").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/summary").hasRole("ADMIN")

                                // Swagger / OpenAPI - permitir acceso público
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/index.html",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs",
                                        "/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**"
                                ).permitAll()
                                .requestMatchers("/favicon.ico").permitAll()

                                // Actuator security - Solo health y info son públicos
                                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                                .requestMatchers("/actuator/**").hasRole("ADMIN")

                                // Todo lo demás requiere autenticación
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