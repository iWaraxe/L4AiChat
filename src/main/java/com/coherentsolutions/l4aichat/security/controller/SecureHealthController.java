package com.coherentsolutions.l4aichat.security.controller;

import com.coherentsolutions.l4aichat.common.dto.HealthResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecureHealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> details = Map.of(
            "features", "JWT Authentication and Authorization",
            "endpoints", "/api/auth/*, /api/security/*",
            "security", "Spring Security with JWT",
            "authenticated", auth != null && auth.isAuthenticated(),
            "principal", auth != null ? auth.getName() : "anonymous",
            "authorities", auth != null ? auth.getAuthorities().toString() : "none"
        );
        
        return HealthResponse.healthy("Security-JWT-Auth", details);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> userEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return Map.of(
            "message", "Hello User!",
            "user", auth.getName(),
            "authorities", auth.getAuthorities(),
            "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return Map.of(
            "message", "Hello Admin!",
            "user", auth.getName(),
            "authorities", auth.getAuthorities(),
            "timestamp", System.currentTimeMillis(),
            "adminFeatures", "User management, System configuration, Security settings"
        );
    }

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return Map.of(
            "username", auth.getName(),
            "authorities", auth.getAuthorities(),
            "authenticated", auth.isAuthenticated(),
            "details", auth.getDetails()
        );
    }
}