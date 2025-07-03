package com.coherentsolutions.l4aichat.s1multiturn.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S1 Multi-turn module.
 */
@RestController
@RequestMapping("/api/s1/health")
public class S1HealthController {

    private final ChatModel chatModel;

    public S1HealthController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            // Basic check that ChatModel is available
            boolean chatModelAvailable = chatModel != null;
            
            Map<String, Object> details = Map.of(
                "features", "Single-turn and multi-turn conversations",
                "endpoints", "/api/s1/chat/*",
                "chatModel", chatModelAvailable ? "available" : "unavailable",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S1-MultiTurn", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S1-MultiTurn", e.getMessage());
        }
    }
}