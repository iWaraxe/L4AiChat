package com.coherentsolutions.l4aichat.s6advanced.dto;

public record ChatResponse(String message, String conversationId) {
    // No-args constructor for frameworks like Jackson
    public ChatResponse() {
        this("", "");
    }
}