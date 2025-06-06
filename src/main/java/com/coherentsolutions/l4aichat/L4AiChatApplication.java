package com.coherentsolutions.l4aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main Spring Boot application for L4 AI Chat project.
 * This application enables all lecture sections to work together.
 */
@SpringBootApplication
@EnableScheduling
public class L4AiChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(L4AiChatApplication.class, args);
    }

    /**
     * Configure CORS to allow frontend applications to access the API
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