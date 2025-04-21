package com.coherentsolutions.l4aichat.s3context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Configuration for JDBC ChatMemory
 * This is only activated when the "jdbc" profile is active
 */
@Configuration
@Profile("jdbc")
public class JdbcConfig {

    /**
     * Creates an in-memory H2 database for demo purposes
     * In a real application, you would use a persistent database
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    /**
     * Creates a JdbcTemplate using the dataSource
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Initialize schema if it doesn't exist
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS ai_chat_memory (
                id VARCHAR(255) PRIMARY KEY,
                session_id VARCHAR(255) NOT NULL,
                type VARCHAR(50) NOT NULL,
                role VARCHAR(50) NOT NULL,
                content CLOB,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        return jdbcTemplate;
    }
}