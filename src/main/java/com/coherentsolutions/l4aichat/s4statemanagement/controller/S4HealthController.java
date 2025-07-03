package com.coherentsolutions.l4aichat.s4statemanagement.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import com.coherentsolutions.l4aichat.s4statemanagement.service.ConversationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S4 State Management module.
 */
@RestController
@RequestMapping("/api/s4/health")
public class S4HealthController {

    private final ChatClient chatClient;
    private final ConversationService conversationService;

    public S4HealthController(ChatClient chatClient, ConversationService conversationService) {
        this.chatClient = chatClient;
        this.conversationService = conversationService;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Advanced state management with conversation history and cleanup",
                "endpoints", "/api/s4/chat/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "conversationService", conversationService != null ? "available" : "unavailable",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S4-StateManagement", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S4-StateManagement", e.getMessage());
        }
    }
}