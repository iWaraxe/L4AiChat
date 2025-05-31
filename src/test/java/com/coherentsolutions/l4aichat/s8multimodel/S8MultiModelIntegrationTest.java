package com.coherentsolutions.l4aichat.s8multimodel;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MultiModelApplication.class)
@AutoConfigureMockMvc
class S8MultiModelIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldCompareMultipleModels() throws Exception {
        String comparisonRequestJson = """
            {
                "query": "Explain the benefits of microservices architecture",
                "models": ["gpt-4", "gpt-3.5-turbo", "gpt-4-turbo"],
                "includeMetrics": true
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(comparisonRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.responses").isArray())
                .andExpect(jsonPath("$.responses[0].model").value("gpt-4"))
                .andExpect(jsonPath("$.responses[0].response").value("Test AI response"))
                .andExpect(jsonPath("$.responses[1].model").value("gpt-3.5-turbo"))
                .andExpect(jsonPath("$.responses[2].model").value("gpt-4-turbo"))
                .andExpect(jsonPath("$.comparison.metrics").exists())
                .andExpect(jsonPath("$.comparison.summary").exists());
    }

    @Test
    void shouldRouteToOptimalModel() throws Exception {
        String routingRequestJson = """
            {
                "query": "What is 2 + 2?",
                "criteria": {
                    "priority": "cost",
                    "maxLatency": 5000,
                    "minQuality": 0.8
                }
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routingRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.selectedModel").exists())
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.routingReason").exists())
                .andExpect(jsonPath("$.metrics.cost").exists())
                .andExpect(jsonPath("$.metrics.estimatedLatency").exists());
    }

    @Test
    void shouldHandleModelFailover() throws Exception {
        String failoverRequestJson = """
            {
                "query": "Complex query that might fail",
                "primaryModel": "gpt-4",
                "fallbackModels": ["gpt-4-turbo", "gpt-3.5-turbo"],
                "maxRetries": 2
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/failover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(failoverRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.usedModel").exists())
                .andExpect(jsonPath("$.attempts").exists())
                .andExpect(jsonPath("$.failoverOccurred").exists());
    }

    @Test
    void shouldSupportParallelProcessing() throws Exception {
        String parallelRequestJson = """
            {
                "queries": [
                    {
                        "id": "q1",
                        "text": "Explain REST APIs",
                        "preferredModel": "gpt-4"
                    },
                    {
                        "id": "q2", 
                        "text": "What is OAuth?",
                        "preferredModel": "gpt-3.5-turbo"
                    },
                    {
                        "id": "q3",
                        "text": "How does JWT work?",
                        "preferredModel": "gpt-4-turbo"
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/parallel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(parallelRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].queryId").value("q1"))
                .andExpect(jsonPath("$.results[0].response").value("Test AI response"))
                .andExpect(jsonPath("$.results[0].model").value("gpt-4"))
                .andExpect(jsonPath("$.totalProcessingTime").exists())
                .andExpect(jsonPath("$.parallelExecuted").value(true));
    }

    @Test
    void shouldDemonstrateModelSpecificFeatures() throws Exception {
        String featuresRequestJson = """
            {
                "query": "Analyze this code for potential issues",
                "codeSnippet": "public void process() { System.out.println(\"Hello\"); }",
                "modelCapabilities": {
                    "gpt-4": ["code_analysis", "security_review"],
                    "gpt-3.5-turbo": ["basic_review"],
                    "gpt-4-turbo": ["code_analysis", "performance_optimization"]
                }
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content(featuresRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.selectedModels").isArray())
                .andExpect(jsonPath("$.analyses").isArray())
                .andExpect(jsonPath("$.consolidatedResult").exists())
                .andExpect(jsonPath("$.capabilityMapping").exists());
    }

    @Test
    void shouldHandleConversationalMultiModel() throws Exception {
        String conversationId = createTestConversationId();
        
        String conversationalRequestJson = """
            {
                "message": "Start a discussion about software architecture",
                "modelStrategy": "adaptive",
                "contextAware": true
            }
            """;

        // Start conversation
        mockMvc.perform(post("/api/s8/multimodel/conversation/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversationalRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.selectedModel").exists());

        // Continue conversation
        String followUpJson = """
            {
                "message": "What about scalability patterns?",
                "modelStrategy": "adaptive"
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/conversation/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(followUpJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.modelSwitched").exists());
    }

    @Test
    void shouldProvideModelAnalytics() throws Exception {
        mockMvc.perform(get("/api/s8/multimodel/analytics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.modelUsage").isArray())
                .andExpect(jsonPath("$.modelUsage[0].model").exists())
                .andExpect(jsonPath("$.modelUsage[0].requestCount").exists())
                .andExpect(jsonPath("$.modelUsage[0].averageLatency").exists())
                .andExpect(jsonPath("$.modelUsage[0].successRate").exists())
                .andExpect(jsonPath("$.totalRequests").exists())
                .andExpect(jsonPath("$.averageCost").exists());
    }

    @Test
    void shouldSupportModelConfiguration() throws Exception {
        String configRequestJson = """
            {
                "model": "gpt-4",
                "configuration": {
                    "temperature": 0.7,
                    "maxTokens": 1000,
                    "timeout": 30000,
                    "retryAttempts": 3
                }
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/configure")
                .contentType(MediaType.APPLICATION_JSON)
                .content(configRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("gpt-4"))
                .andExpect(jsonPath("$.applied").value(true))
                .andExpect(jsonPath("$.configuration.temperature").value(0.7))
                .andExpect(jsonPath("$.configuration.maxTokens").value(1000));
    }

    @Test
    void shouldValidateMultiModelRequest() throws Exception {
        // Test missing required fields
        mockMvc.perform(post("/api/s8/multimodel/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Query and models are required"));
    }

    @Test
    void shouldHandleInvalidModelNames() throws Exception {
        String invalidModelRequestJson = """
            {
                "query": "Test query",
                "models": ["gpt-4", "invalid-model", "gpt-3.5-turbo"]
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidModelRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid model: invalid-model"));
    }

    @Test
    void shouldHandleLoadBalancing() throws Exception {
        String loadBalanceRequestJson = """
            {
                "queries": [
                    "Query 1", "Query 2", "Query 3", "Query 4", "Query 5"
                ],
                "strategy": "round_robin",
                "availableModels": ["gpt-4", "gpt-3.5-turbo", "gpt-4-turbo"]
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/load-balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loadBalanceRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results").hasJsonPath())
                .andExpect(jsonPath("$.loadDistribution").exists())
                .andExpect(jsonPath("$.strategy").value("round_robin"));
    }

    @Test
    void shouldSupportCustomModelEndpoints() throws Exception {
        String customEndpointRequestJson = """
            {
                "query": "Test custom endpoint",
                "customModels": [
                    {
                        "name": "custom-llm",
                        "endpoint": "https://api.custom-provider.com/chat",
                        "apiKey": "custom-key"
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/s8/multimodel/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customEndpointRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Test AI response"))
                .andExpect(jsonPath("$.usedModel").value("custom-llm"))
                .andExpect(jsonPath("$.endpointTested").value(true));
    }

    @Test
    void shouldProvideModelHealthStatus() throws Exception {
        mockMvc.perform(get("/api/s8/multimodel/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.models").isArray())
                .andExpect(jsonPath("$.models[0].name").exists())
                .andExpect(jsonPath("$.models[0].status").exists())
                .andExpect(jsonPath("$.models[0].lastChecked").exists())
                .andExpect(jsonPath("$.models[0].averageLatency").exists())
                .andExpect(jsonPath("$.overallHealth").exists());
    }
}