package com.coherentsolutions.l4aichat.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Main application health endpoint providing overview of all modules.
 */
@RestController
@RequestMapping("/api/health")
public class ApplicationHealthController {

    @GetMapping
    public HealthResponse health() {
        try {
            Map<String, Object> details = Map.of(
                "description", "L4 AI Chat - Spring AI Educational Course Project",
                "springAiVersion", "1.0.0",
                "springBootVersion", "3.4.4",
                "javaVersion", "21",
                "modules", List.of(
                    Map.of("module", "s1-multiturn", "endpoint", "/api/s1/health"),
                    Map.of("module", "s2-components", "endpoint", "/api/s2/health"),
                    Map.of("module", "s3-context", "endpoint", "/api/s3/health"),
                    Map.of("module", "s4-statemanagement", "endpoint", "/api/s4/health"),
                    Map.of("module", "s5-chatbot", "endpoint", "/api/s5/health"),
                    Map.of("module", "s6-advanced", "endpoint", "/api/s6/health"),
                    Map.of("module", "s7-advisors", "endpoint", "/api/s7/health"),
                    Map.of("module", "s8-multimodel", "endpoint", "/api/s8/health"),
                    Map.of("module", "s9-templates", "endpoint", "/api/s9/health")
                ),
                "features", List.of(
                    "Multi-turn conversations",
                    "ChatClient vs ChatModel demonstrations", 
                    "Context management with advisors",
                    "Advanced state management",
                    "REST API with streaming",
                    "Advanced chat patterns",
                    "Custom advisor implementations",
                    "Multiple model configurations",
                    "Advanced prompt templating"
                )
            );
            
            return HealthResponse.healthy("L4AiChat-Application", details);
        } catch (Exception e) {
            return HealthResponse.unhealthy("L4AiChat-Application", e.getMessage());
        }
    }
}