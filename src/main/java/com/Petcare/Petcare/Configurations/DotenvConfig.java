package com.Petcare.Petcare.Configurations;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration class to load environment variables from .env file.
 * This allows sensitive configuration to be stored outside the codebase.
 * 
 * The .env file should contain:
 * - Database credentials
 * - JWT secret
 * - Email credentials
 * - API keys
 * 
 * IMPORTANT: .env file is excluded from git via .gitignore
 */
@Configuration
public class DotenvConfig {

    private static final Logger log = LoggerFactory.getLogger(DotenvConfig.class);

    /**
     * Load .env file and expose its variables as system properties.
     * This allows Spring to use ${VAR_NAME} placeholders.
     */
    @Bean
    public Dotenv dotenv() {
        Dotenv dotenv = null;
        
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load();
            
            // Expose all variables as system properties for Spring
            if (dotenv != null) {
                Map<String, String> envVars = dotenv.entries().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue()
                        ));
                
                // Set system properties (only if not already set)
                envVars.forEach((key, value) -> {
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                });
                log.info("Loaded {} environment variables from .env file", envVars.size());
            }
        } catch (Exception e) {
            log.warn(".env file not found or invalid - using system environment variables");
        }
        
        return dotenv;
    }
}
