package com.coherentsolutions.l4aichat.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard health check response format for all modules.
 */
public record HealthResponse(
    String status,
    String module,
    LocalDateTime timestamp,
    Map<String, Object> details
) {
    
    public static HealthResponse healthy(String module) {
        return new HealthResponse(
            "UP", 
            module, 
            LocalDateTime.now(), 
            Map.of("description", module + " module is operational")
        );
    }
    
    public static HealthResponse healthy(String module, Map<String, Object> details) {
        return new HealthResponse("UP", module, LocalDateTime.now(), details);
    }
    
    public static HealthResponse unhealthy(String module, String reason) {
        return new HealthResponse(
            "DOWN", 
            module, 
            LocalDateTime.now(), 
            Map.of("error", reason)
        );
    }
}