package com.coherentsolutions.l4aichat.s5chatbot.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import com.coherentsolutions.l4aichat.s5chatbot.service.ChatbotService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S5 Chatbot module.
 */
@RestController
@RequestMapping("/api/s5/health")
public class S5HealthController {

    private final ChatClient chatClient;
    private final ChatbotService chatbotService;

    public S5HealthController(ChatClient chatClient, ChatbotService chatbotService) {
        this.chatClient = chatClient;
        this.chatbotService = chatbotService;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Complete REST API chatbot with streaming and error handling",
                "endpoints", "/api/s5/chat/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "chatbotService", chatbotService != null ? "available" : "unavailable",
                "streaming", "enabled",
                "cors", "configured",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S5-Chatbot", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S5-Chatbot", e.getMessage());
        }
    }
}