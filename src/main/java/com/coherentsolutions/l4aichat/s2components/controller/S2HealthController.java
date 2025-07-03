package com.coherentsolutions.l4aichat.s2components.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S2 Components module.
 */
@RestController
@RequestMapping("/api/s2/health")
public class S2HealthController {

    private final ChatModel chatModel;
    private final ChatClient chatClient;

    public S2HealthController(ChatModel chatModel, ChatClient chatClient) {
        this.chatModel = chatModel;
        this.chatClient = chatClient;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "ChatClient vs ChatModel demonstrations, message types, structured output",
                "endpoints", "/api/s2/client/*, /api/s2/model/*, /api/s2/messages/*, /api/structured/*",
                "chatModel", chatModel != null ? "available" : "unavailable",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S2-Components", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S2-Components", e.getMessage());
        }
    }
}