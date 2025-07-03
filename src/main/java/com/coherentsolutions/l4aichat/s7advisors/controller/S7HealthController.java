package com.coherentsolutions.l4aichat.s7advisors.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import com.coherentsolutions.l4aichat.s7advisors.service.AdvisorDemoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S7 Advisors module.
 */
@RestController
@RequestMapping("/api/s7/health")
public class S7HealthController {

    private final ChatClient chatClient;
    private final AdvisorDemoService advisorDemoService;

    public S7HealthController(ChatClient chatClient, AdvisorDemoService advisorDemoService) {
        this.chatClient = chatClient;
        this.advisorDemoService = advisorDemoService;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Custom advisor implementations with multiple ChatClient configurations",
                "endpoints", "/api/s7/advisors/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "advisorDemoService", advisorDemoService != null ? "available" : "unavailable",
                "advisorTypes", "MessageChatMemoryAdvisor, custom advisors",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S7-Advisors", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S7-Advisors", e.getMessage());
        }
    }
}