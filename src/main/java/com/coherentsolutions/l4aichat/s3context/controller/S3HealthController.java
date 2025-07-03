package com.coherentsolutions.l4aichat.s3context.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S3 Context module.
 */
@RestController
@RequestMapping("/api/s3/health")
public class S3HealthController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public S3HealthController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Context management with MessageChatMemoryAdvisor, in-memory and JDBC storage",
                "endpoints", "/api/s3/chat/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "chatMemory", chatMemory != null ? "available" : "unavailable",
                "memoryType", chatMemory != null ? chatMemory.getClass().getSimpleName() : "unknown",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S3-Context", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S3-Context", e.getMessage());
        }
    }
}