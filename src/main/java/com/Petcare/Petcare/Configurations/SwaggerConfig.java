package com.Petcare.Petcare.Configurations;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Configuration for Swagger/OpenAPI documentation with JWT authentication.
 * 
 * This configuration:
 * - Adds JWT bearer token authentication to Swagger UI
 * - Documents all API endpoints
 * - Provides contact information
 * - Automatically detects server URL from request in production
 */
@Configuration
public class SwaggerConfig {

    @Value("${petcare.api.base-url:http://localhost:8088}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI(HttpServletRequest request) {
        // Detect production URL from request headers if running behind proxy
        String serverUrl = detectServerUrl(request);
        
        return new OpenAPI()
                .info(new Info()
                        .title("Petcare API")
                        .version("1.0")
                        .description("""
                                ## Petcare Application API
                                
                                This API provides endpoints for:
                                - User authentication and management
                                - Pet management
                                - Booking services
                                - Payment processing
                                - Invoice generation
                                
                                ## Authentication
                                
                                Most endpoints require authentication using JWT Bearer token.
                                To authenticate:
                                1. Use `/api/users/login` or `/api/users/register` to get a token
                                2. Click the **Authorize** button below
                                3. Enter the token in the format: `Bearer <your-token>`
                                
                                ## Rate Limiting
                                
                                API requests are limited to 5 requests per minute per IP/user.
                                """)
                        .contact(new Contact()
                                .name("Petcare Team")
                                .email("support@petcare.com")))
                .servers(List.of(
                        new Server().url(serverUrl).description("Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                    ### How to get a token:
                                    
                                    1. **Login**: POST `/api/users/login`
                                       ```json
                                       {
                                         "email": "your@email.com",
                                         "password": "your-password"
                                       }
                                       ```
                                    
                                    2. **Register**: POST `/api/users/register`
                                       ```json
                                       {
                                         "firstname": "John",
                                         "lastname": "Doe",
                                         "email": "your@email.com",
                                         "password": "secure-password",
                                         "address": "123 Main St",
                                         "phoneNumber": "+1234567890"
                                       }
                                       ```
                                    
                                    3. **Use the token**: Copy the `token` from the response and paste it below in the format:
                                       ```
                                       Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                       ```
                                    """)));
    }

    /**
     * Detects the server URL from request headers (for production behind proxy)
     * or falls back to configured base-url.
     */
    private String detectServerUrl(HttpServletRequest request) {
        // Check for forwarded headers (set by reverse proxy/load balancer)
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String forwardedPort = request.getHeader("X-Forwarded-Port");
        String forwardedPrefix = request.getHeader("X-Forwarded-Prefix");

        if (forwardedHost != null) {
            StringBuilder url = new StringBuilder();
            
            // Determine protocol
            if (forwardedProto != null) {
                url.append(forwardedProto).append("://");
            } else {
                url.append(request.getScheme()).append("://");
            }
            
            url.append(forwardedHost);
            
            // Determine port
            if (forwardedPort != null && !forwardedPort.equals("80") && !forwardedPort.equals("443")) {
                url.append(":").append(forwardedPort);
            }
            
            // Add prefix if present
            if (forwardedPrefix != null && !forwardedPrefix.isEmpty()) {
                url.append(forwardedPrefix);
            }
            
            return url.toString();
        }

        // Fallback to configured base-url
        return baseUrl;
    }
}
