package com.coherentsolutions.l4aichat.s6advanced;

import com.coherentsolutions.l4aichat.L4AiChatApplication;
import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.coherentsolutions.l4aichat.L4AiChatApplication.class)
@AutoConfigureMockMvc
class S6AdvancedIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldHandleAdvancedChatFeatures() throws Exception {
        mockMvc.perform(post("/api/s6/chat/advanced")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAdvancedChatRequestJson("Tell me about quantum computing", "analytical", "medium")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.metadata.responseStyle").value("analytical"))
                .andExpect(jsonPath("$.metadata.complexity").value("medium"))
                .andExpect(jsonPath("$.metadata.timestamp").exists());
    }

    @Test
    void shouldGenerateStructuredResponse() throws Exception {
        String structuredRequestJson = """
            {
                "query": "Analyze the benefits of microservices architecture",
                "outputFormat": "structured",
                "includeMetadata": true
            }
            """;

        mockMvc.perform(post("/api/s6/chat/structured")
                .contentType(MediaType.APPLICATION_JSON)
                .content(structuredRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.analysis").exists())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.keyPoints").isArray())
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(jsonPath("$.metadata.analysisType").exists());
    }

    @Test
    void shouldSupportConversationalAnalysis() throws Exception {
        String conversationId = createTestConversationId();
        
        // Start conversation with context
        mockMvc.perform(post("/api/s6/chat/conversational/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createConversationalRequestJson("I'm working on a Spring Boot project", "development")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.context.domain").value("development"));
        
        // Continue with follow-up
        mockMvc.perform(post("/api/s6/chat/conversational/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createConversationalRequestJson("What testing strategies should I use?", "development")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId));
    }

    @Test
    void shouldHandleMultiStepAnalysis() throws Exception {
        String multiStepRequestJson = """
            {
                "problem": "Design a scalable e-commerce system",
                "steps": [
                    "requirements_analysis",
                    "architecture_design",
                    "technology_selection",
                    "implementation_plan"
                ],
                "depth": "detailed"
            }
            """;

        mockMvc.perform(post("/api/s6/chat/multi-step")
                .contentType(MediaType.APPLICATION_JSON)
                .content(multiStepRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps[0].name").value("requirements_analysis"))
                .andExpect(jsonPath("$.steps[0].result").exists())
                .andExpect(jsonPath("$.overall_analysis").exists())
                .andExpect(jsonPath("$.recommendations").isArray());
    }

    @Test
    void shouldSupportAdvancedStreaming() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s6/chat/stream/advanced/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createStreamingRequestJson("Write a comprehensive guide on microservices", "progressive")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("X-Stream-Type", "progressive"));
    }

    @Test
    void shouldHandleContextAwareResponses() throws Exception {
        String contextRequestJson = """
            {
                "message": "How can I optimize this code?",
                "context": {
                    "language": "java",
                    "framework": "spring",
                    "domain": "web_development",
                    "experience_level": "intermediate"
                }
            }
            """;

        mockMvc.perform(post("/api/s6/chat/context-aware")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contextRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.appliedContext.language").value("java"))
                .andExpect(jsonPath("$.appliedContext.framework").value("spring"))
                .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    void shouldValidateAdvancedChatRequest() throws Exception {
        // Test missing required fields
        mockMvc.perform(post("/api/s6/chat/advanced")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldValidateStructuredOutputRequest() throws Exception {
        // Test invalid output format
        String invalidRequestJson = """
            {
                "query": "Test query",
                "outputFormat": "invalid_format"
            }
            """;

        mockMvc.perform(post("/api/s6/chat/structured")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid output format"));
    }

    @Test
    void shouldHandleComplexQueryAnalysis() throws Exception {
        String complexQueryJson = """
            {
                "query": "Compare and contrast different architectural patterns for distributed systems",
                "analysisType": "comparative",
                "includeExamples": true,
                "includeTradeoffs": true,
                "targetAudience": "senior_developers"
            }
            """;

        mockMvc.perform(post("/api/s6/chat/complex-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(complexQueryJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.analysis.type").value("comparative"))
                .andExpect(jsonPath("$.comparison_matrix").isArray())
                .andExpect(jsonPath("$.examples").isArray())
                .andExpect(jsonPath("$.tradeoffs").isArray())
                .andExpect(jsonPath("$.recommendations").exists());
    }

    @Test
    void shouldSupportInteractiveMode() throws Exception {
        String conversationId = createTestConversationId();
        
        String interactiveRequestJson = """
            {
                "message": "Start a code review session",
                "mode": "interactive",
                "sessionType": "code_review",
                "parameters": {
                    "language": "java",
                    "focus_areas": ["performance", "security", "maintainability"]
                }
            }
            """;

        mockMvc.perform(post("/api/s6/chat/interactive/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(interactiveRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.mode").value("interactive"))
                .andExpect(jsonPath("$.sessionType").value("code_review"))
                .andExpect(jsonPath("$.nextSteps").isArray())
                .andExpect(jsonPath("$.response").value("Test AI response"));
    }

    @Test
    void shouldHandleAdvancedErrorScenarios() throws Exception {
        // Test timeout handling
        String timeoutRequestJson = """
            {
                "query": "Very complex analysis that might timeout",
                "timeout": 1
            }
            """;

        mockMvc.perform(post("/api/s6/chat/advanced")
                .contentType(MediaType.APPLICATION_JSON)
                .content(timeoutRequestJson))
                .andExpect(status().isRequestTimeout())
                .andExpect(jsonPath("$.error").value("TIMEOUT_ERROR"))
                .andExpect(jsonPath("$.message").value("Request timed out"));
    }

    // Helper methods for creating test JSON
    private String createAdvancedChatRequestJson(String message, String style, String complexity) {
        return String.format("""
            {
                "message": "%s",
                "responseStyle": "%s",
                "complexity": "%s"
            }
            """, message, style, complexity);
    }

    private String createConversationalRequestJson(String message, String domain) {
        return String.format("""
            {
                "message": "%s",
                "context": {
                    "domain": "%s"
                }
            }
            """, message, domain);
    }

    private String createStreamingRequestJson(String message, String streamType) {
        return String.format("""
            {
                "message": "%s",
                "streamType": "%s"
            }
            """, message, streamType);
    }
}