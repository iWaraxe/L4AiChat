package com.coherentsolutions.l4aichat.s4statemanagement.model;

/**
 * DTO for chat responses to clients.
 */
public record ChatResponse(String message, String conversationId, String role) {

    // For frameworks like Jackson that want a no-arg constructor
    public ChatResponse() {
        this("", "", "ASSISTANT");
    }
}
