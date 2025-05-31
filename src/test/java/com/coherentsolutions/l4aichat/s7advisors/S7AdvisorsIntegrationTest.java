package com.coherentsolutions.l4aichat.s7advisors;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AdvisorsApplication.class)
@AutoConfigureMockMvc
class S7AdvisorsIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldDemonstrateLoggingAdvisor() throws Exception {
        mockMvc.perform(post("/api/s7/advisors/demo/logging")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Test logging advisor functionality")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.advisorInfo.type").value("logging"))
                .andExpect(jsonPath("$.advisorInfo.logged").value(true))
                .andExpect(jsonPath("$.metadata.requestId").exists());
    }

    @Test
    void shouldDemonstrateRe2Advisor() throws Exception {
        String complexQueryJson = """
            {
                "query": "Solve this complex problem: How to implement a distributed cache with consistency guarantees?",
                "useRe2": true
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/re2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(complexQueryJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.advisorInfo.type").value("re2"))
                .andExpect(jsonPath("$.advisorInfo.iterations").exists())
                .andExpect(jsonPath("$.metadata.processingSteps").isArray());
    }

    @Test
    void shouldDemonstrateSafetyAdvisor() throws Exception {
        String safetyTestJson = """
            {
                "message": "How do I secure my API endpoints?",
                "enableSafetyCheck": true
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/safety")
                .contentType(MediaType.APPLICATION_JSON)
                .content(safetyTestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.advisorInfo.type").value("safety"))
                .andExpect(jsonPath("$.advisorInfo.safetyScore").exists())
                .andExpect(jsonPath("$.advisorInfo.flaggedContent").value(false));
    }

    @Test
    void shouldDemonstrateAdvisorChaining() throws Exception {
        String chainedRequestJson = """
            {
                "message": "Explain machine learning algorithms",
                "advisors": ["safety", "logging", "re2"],
                "chainOrder": "sequential"
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/chained")
                .contentType(MediaType.APPLICATION_JSON)
                .content(chainedRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.advisorChain").isArray())
                .andExpect(jsonPath("$.advisorChain[0].type").value("safety"))
                .andExpect(jsonPath("$.advisorChain[1].type").value("logging"))
                .andExpect(jsonPath("$.advisorChain[2].type").value("re2"))
                .andExpect(jsonPath("$.metadata.totalProcessingTime").exists());
    }

    @Test
    void shouldHandleCustomAdvisorConfiguration() throws Exception {
        String customConfigJson = """
            {
                "message": "Test custom advisor configuration",
                "advisorConfig": {
                    "logging": {
                        "logLevel": "DEBUG",
                        "includeTimestamps": true
                    },
                    "safety": {
                        "strictMode": true,
                        "customRules": ["no_personal_info", "no_code_injection"]
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/custom-config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customConfigJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.appliedConfiguration.logging.logLevel").value("DEBUG"))
                .andExpect(jsonPath("$.appliedConfiguration.safety.strictMode").value(true));
    }

    @Test
    void shouldDemonstrateConversationalAdvisors() throws Exception {
        String conversationId = createTestConversationId();
        
        String conversationalRequestJson = """
            {
                "message": "Remember my name is Alice and I work at TechCorp",
                "advisors": ["memory", "context", "logging"]
            }
            """;

        // First message to establish context
        mockMvc.perform(post("/api/s7/advisors/conversational/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversationalRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId));

        // Follow-up message to test context retention
        String followUpJson = """
            {
                "message": "What company do I work for?",
                "advisors": ["memory", "context"]
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/conversational/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(followUpJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.contextUsed").value(true));
    }

    @Test
    void shouldHandleAdvisorErrors() throws Exception {
        String errorProneRequestJson = """
            {
                "message": "Test error handling",
                "advisors": ["invalid_advisor"],
                "failOnAdvisorError": false
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/error-handling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(errorProneRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.advisorErrors").isArray())
                .andExpect(jsonPath("$.advisorErrors[0].advisor").value("invalid_advisor"))
                .andExpect(jsonPath("$.advisorErrors[0].error").exists());
    }

    @Test
    void shouldValidateAdvisorRequest() throws Exception {
        // Test missing message
        mockMvc.perform(post("/api/s7/advisors/demo/logging")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Message is required"));
    }

    @Test
    void shouldHandleAdvisorPerformanceMetrics() throws Exception {
        String performanceTestJson = """
            {
                "message": "Test advisor performance metrics",
                "advisors": ["logging", "safety", "re2"],
                "includeMetrics": true
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(performanceTestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.performance.totalTime").exists())
                .andExpect(jsonPath("$.performance.advisorTimes").isArray())
                .andExpect(jsonPath("$.performance.advisorTimes[0].advisor").exists())
                .andExpect(jsonPath("$.performance.advisorTimes[0].duration").exists());
    }

    @Test
    void shouldSupportAdvisorOrdering() throws Exception {
        String orderingTestJson = """
            {
                "message": "Test advisor ordering",
                "advisors": [
                    {"name": "safety", "order": 1},
                    {"name": "logging", "order": 3},
                    {"name": "re2", "order": 2}
                ]
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/ordering")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderingTestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.executionOrder[0]").value("safety"))
                .andExpect(jsonPath("$.executionOrder[1]").value("re2"))
                .andExpect(jsonPath("$.executionOrder[2]").value("logging"));
    }

    @Test
    void shouldHandleConditionalAdvisors() throws Exception {
        String conditionalRequestJson = """
            {
                "message": "How do I handle user authentication?",
                "conditionalAdvisors": {
                    "safety": {
                        "condition": "contains_security_terms",
                        "enabled": true
                    },
                    "re2": {
                        "condition": "complex_query",
                        "threshold": 0.7
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/demo/conditional")
                .contentType(MediaType.APPLICATION_JSON)
                .content(conditionalRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.activatedAdvisors").isArray())
                .andExpect(jsonPath("$.conditionResults.safety").value(true));
    }

    @Test
    void shouldDemonstrateStreamingWithAdvisors() throws Exception {
        String conversationId = createTestConversationId();
        
        String streamingRequestJson = """
            {
                "message": "Write a detailed explanation of Spring Security",
                "advisors": ["logging", "safety"],
                "streaming": true
            }
            """;

        mockMvc.perform(post("/api/s7/advisors/stream/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(streamingRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(header().string("Cache-Control", "no-cache"));
    }
}