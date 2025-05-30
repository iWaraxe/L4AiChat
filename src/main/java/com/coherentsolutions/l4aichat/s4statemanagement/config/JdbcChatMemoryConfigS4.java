package com.coherentsolutions.l4aichat.s4statemanagement.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
     * @return A persistent chat memory implementation
     */
    @Bean
    public ChatMemory chatMemory() {
        // Spring AI 1.0.0 will auto-configure the JDBC repository when the starter is present
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
}