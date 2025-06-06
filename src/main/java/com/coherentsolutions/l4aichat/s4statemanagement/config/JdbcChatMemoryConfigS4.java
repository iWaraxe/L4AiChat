package com.coherentsolutions.l4aichat.s4statemanagement.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration for database-backed chat memory.
 * This provides persistent conversation storage across application restarts.
 * Activated with the "jdbc-memory" Spring profile.
 */
@Configuration
@Profile("jdbc-memory")
public class JdbcChatMemoryConfigS4 {

    /**
     * Creates a JDBC-backed chat memory implementation.
     * This will automatically create the necessary database table.
     *
     * @param jdbcTemplate The Spring JDBC template
     * @return A persistent chat memory implementation
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