package com.coherentsolutions.l4aichat.testutils;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests providing common configuration and utilities.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "spring.ai.openai.base-url=http://localhost:8080/mock-ai",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.com.coherentsolutions.l4aichat=DEBUG",
    "spring.jpa.show-sql=false",
    "spring.h2.console.enabled=false"
})
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Common setup for all integration tests
        setupTestData();
    }

    protected void setupTestData() {
        // Override in subclasses if needed
    }

    /**
     * Helper method to create a valid conversation ID for testing
     */
    protected String createTestConversationId() {
        return "test-conversation-" + System.currentTimeMillis();
    }

    /**
     * Helper method to create test chat request JSON
     */
    protected String createChatRequestJson(String message) {
        return String.format("{\"message\": \"%s\"}", message);
    }
}