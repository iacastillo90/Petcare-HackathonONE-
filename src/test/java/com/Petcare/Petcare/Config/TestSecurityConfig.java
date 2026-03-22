package com.Petcare.Petcare.Config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration for disabling security in integration tests.
 * 
 * <p>This config is imported by @WebMvcTest classes to bypass JWT authentication
 * and authorization, allowing direct endpoint testing without mock users.
 * 
 * <p>Usage:
 * <pre>
 * @WebMvcTest(BookingController.class)
 * @Import(TestSecurityConfig.class)
 * class BookingControllerTest { ... }
 * </pre>
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
