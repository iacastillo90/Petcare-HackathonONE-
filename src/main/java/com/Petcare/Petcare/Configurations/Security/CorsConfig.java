package com.Petcare.Petcare.Configurations.Security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de CORS para permitir solicitudes de origen cruzado.
 *
 * NOTA: Se usa CorsConfigurationSource bean en lugar de WebMvcConfigurer
 * para que Spring Security lo utilice correctamente en el SecurityFilterChain.
 * Esto evita que las peticiones preflight (OPTIONS) sean bloqueadas por el filtro de seguridad.
 */
@Configuration
public class CorsConfig {

    // Dominios permitidos - el frontend en Traefik y Swagger local
    private static final String TRAEFIK_FRONTEND_URL = "http://petcare-frontend-ppd6vc-370d89-144-225-147-45.traefik.me";
    private static final String LOCALHOST_3000 = "http://localhost:3000";
    private static final String LOCALHOST_5173 = "http://localhost:5173";
    private static final String LOCALHOST_8088 = "http://localhost:8088";
    private static final String LOCALHOST_4200 = "http://localhost:4200";

    /**
     * Configura las políticas de CORS para permitir solicitudes de origen cruzado.
     * Spring Security usa este bean automáticamente cuando se habilita .cors() en SecurityFilterChain.
     *
     * @return CorsConfigurationSource configurado con los orígenes permitidos.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos
        configuration.setAllowedOrigins(List.of(
                TRAEFIK_FRONTEND_URL,
                LOCALHOST_3000,
                LOCALHOST_5173,
                LOCALHOST_8088,
                LOCALHOST_4200
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // Headers permitidos - incluir todos los necesarios para JWT y API
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // Permitir credenciales (cookies, auth headers)
        configuration.setAllowCredentials(true);

        // Cache de preflight por 1 hora
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
