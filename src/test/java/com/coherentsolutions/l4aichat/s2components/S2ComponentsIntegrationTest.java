package com.coherentsolutions.l4aichat.s2components;

import com.coherentsolutions.l4aichat.config.MockTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ComponentsApplication.class)
@AutoConfigureMockMvc
@Import(MockTestConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "logging.level.com.coherentsolutions.l4aichat=DEBUG"
})
class S2ComponentsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }

    @Test
    void shouldLoadApplicationComponents() {
        // Test that Spring context loads with all required components
        // This validates the basic Spring Boot configuration and dependencies
    }
}