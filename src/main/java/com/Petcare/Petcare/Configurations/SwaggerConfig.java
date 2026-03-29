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

/**
 * Configuration for Swagger/OpenAPI documentation with JWT authentication.
 * 
 * This configuration:
 * - Adds JWT bearer token authentication to Swagger UI
 * - Documents all API endpoints
 * - Provides contact information
 * - Uses configured base URL (petcare.api.base-url property)
 */
@Configuration
public class SwaggerConfig {

    @Value("${petcare.api.base-url:http://localhost:8088}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        // Use configured base URL - detectServerUrl removed as it requires HTTP request context
        // which is not available at startup. For production, configure petcare.api.base-url
        String serverUrl = baseUrl;
        
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
}
