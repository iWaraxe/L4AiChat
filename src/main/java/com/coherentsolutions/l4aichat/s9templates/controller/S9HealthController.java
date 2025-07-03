package com.coherentsolutions.l4aichat.s9templates.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import com.coherentsolutions.l4aichat.s9templates.service.PromptTemplateService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for S9 Templates module.
 */
@RestController
@RequestMapping("/api/s9/health")
public class S9HealthController {

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;

    public S9HealthController(ChatClient chatClient, PromptTemplateService promptTemplateService) {
        this.chatClient = chatClient;
        this.promptTemplateService = promptTemplateService;
    }

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "features", "Advanced prompt templating with PromptTemplateService",
                "endpoints", "/api/s9/templates/*",
                "chatClient", chatClient != null ? "available" : "unavailable",
                "promptTemplateService", promptTemplateService != null ? "available" : "unavailable",
                "templateTypes", "6 different templates with parameter substitution",
                "jsonSafeHandling", "enabled",
                "springAiVersion", "1.0.0"
            );
            
            return HealthResponse.healthy("S9-Templates", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("S9-Templates", e.getMessage());
        }
    }
}