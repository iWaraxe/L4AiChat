package com.coherentsolutions.l4aichat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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

    /**
     * Configure ChatClient.Builder bean for all modules
     * This is required for service classes that use ChatClient
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    /**
     * Configure ChatClient bean for health controllers and direct injection
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("You are a helpful AI assistant.")
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .build())
                .build();
    }
}