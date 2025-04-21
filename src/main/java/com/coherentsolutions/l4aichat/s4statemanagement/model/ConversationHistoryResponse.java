package com.coherentsolutions.l4aichat.s4statemanagement.model;

import java.util.List;

/**
 * DTO for returning conversation history.
 */
public record ConversationHistoryResponse(String conversationId, List<ChatResponse> messages) {
    // For frameworks like Jackson that require a no-arg constructor
    public ConversationHistoryResponse() {
        this("", List.of());
    }
}