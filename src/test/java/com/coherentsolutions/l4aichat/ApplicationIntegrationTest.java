package com.coherentsolutions.l4aichat;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test to verify all modules and health endpoints are properly configured
 * and accessible. Uses mocked AI responses to avoid external API dependencies.
 */
class ApplicationIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testApplicationHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("L4AiChat-Application"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void testS1HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S1-MultiTurn"));
    }
    
    @Test
    void testS2HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s2/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S2-Components"));
    }
    
    @Test
    void testS3HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s3/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S3-Context"));
    }
    
    @Test
    void testS4HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s4/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S4-StateManagement"));
    }
    
    @Test
    void testS5HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s5/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S5-Chatbot"));
    }
    
    @Test
    void testS6HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s6/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S6-Advanced"));
    }
    
    @Test
    void testS7HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s7/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S7-Advisors"));
    }
    
    @Test
    void testS8HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s8/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S8-MultiModel"));
    }
    
    @Test
    void testS9HealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/s9/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.module").value("S9-Templates"));
    }
}