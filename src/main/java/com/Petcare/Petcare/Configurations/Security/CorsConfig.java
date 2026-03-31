package com.Petcare.Petcare.Configurations.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Clase de configuración de CORS (Cross-Origin Resource Sharing) a nivel de Spring Security.
 */
@Configuration
public class CorsConfig {

    /**
     * Define la fuente de configuración CORS para que Spring Security la intercepte
     * correctamente en el filtro pre-flight (OPTIONS).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Orígenes permitidos (El frontend exacto)
        configuration.setAllowedOrigins(List.of("https://frontend-pet-403q5exqo-iacastillo90s-projects.vercel.app","http://localhost:5173"));

        // 2. Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 3. Cabeceras permitidas
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // 4. Permitir envío de credenciales (esencial para Axios con withCredentials)
        configuration.setAllowCredentials(true);

        // 5. Aplicar estas reglas a todas las rutas del backend
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
