package com.coherentsolutions.l4aichat.s6advanced.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import com.coherentsolutions.l4aichat.s6advanced.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S6 Advanced module.
 */
@RestController
@RequestMapping("/api/s6/health")
public class S6HealthController {

    private final ChatClient chatClient;
    private final ChatService chatService;

    public S6HealthController(ChatClient chatClient, ChatService chatService) {
        this.chatClient = chatClient;
        this.chatService = chatService;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Advanced chat patterns and structured responses with advisors",
                "endpoints", "/api/s6/chat/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "chatService", chatService != null ? "available" : "unavailable",
                "advisors", "enabled",
                "structuredOutput", "enabled",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S6-Advanced", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S6-Advanced", e.getMessage());
        }
    }
}