package com.Petcare.Petcare.Configurations.Security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase de configuración de Spring para manejar políticas de CORS (Cross-Origin Resource Sharing).
 * Permite solicitudes de origen cruzado desde dominios especificados, facilitando la interacción entre el frontend y el backend.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configura las políticas de CORS para permitir solicitudes de origen cruzado.
     *
     * @param registry El registro de CORS proporcionado por Spring para definir las políticas.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}