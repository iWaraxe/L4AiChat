package com.coherentsolutions.l4aichat.s8multimodel.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import com.coherentsolutions.l4aichat.s8multimodel.service.MultiModelService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S8 Multi-Model module.
 */
@RestController
@RequestMapping("/api/s8/health")
public class S8HealthController {

    private final ChatClient chatClient;
    private final MultiModelService multiModelService;

    public S8HealthController(ChatClient chatClient, MultiModelService multiModelService) {
        this.chatClient = chatClient;
        this.multiModelService = multiModelService;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Multiple model configurations (GPT-4, GPT-3.5 Turbo, GPT-4 Turbo)",
                "endpoints", "/api/s8/models/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "multiModelService", multiModelService != null ? "available" : "unavailable",
                "models", "GPT-4, GPT-3.5-Turbo, GPT-4-Turbo",
                "fallbackSupport", "enabled",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S8-MultiModel", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S8-MultiModel", e.getMessage());
        }
    }
}