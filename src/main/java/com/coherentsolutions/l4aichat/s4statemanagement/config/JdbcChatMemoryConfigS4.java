package com.coherentsolutions.l4aichat.s4statemanagement.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration for chat memory with JDBC profile.
 * Note: In Spring AI 1.0.0, JdbcChatMemory is no longer available.
 * This configuration uses MessageWindowChatMemory instead.
 * For true JDBC persistence, you would need to implement a custom ChatMemoryRepository.
 * Activated with the "jdbc-memory" Spring profile.
 */
@Configuration
@Profile("jdbc-memory")
public class JdbcChatMemoryConfigS4 {

    /**
     * Creates a MessageWindowChatMemory implementation.
     * Note: This is in-memory only. For true JDBC persistence,
     * implement a custom ChatMemoryRepository backed by JDBC.
     *
     * @param jdbcTemplate The Spring JDBC template (available for custom implementations)
     * @return A chat memory implementation
     */
    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
        // In Spring AI 1.0.0, use MessageWindowChatMemory with builder pattern
        // You could implement a custom ChatMemoryRepository using the jdbcTemplate if needed
        return MessageWindowChatMemory.builder()
                .maxMessages(50) // Larger window size for this profile
                .build();
    }
}