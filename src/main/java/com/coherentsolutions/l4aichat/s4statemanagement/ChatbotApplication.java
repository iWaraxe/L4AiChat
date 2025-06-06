package com.coherentsolutions.l4aichat.s4statemanagement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for the s4 chatbot features.
 * Configures CORS and enables scheduled tasks.
 */
@Configuration
@EnableScheduling
public class ChatbotApplication {

    // Removed main method - this is now just a configuration class

    /**
     * CORS configuration to allow frontend access.
     * This is important for web applications hosted on different domains.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000") // Frontend URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }
}