package com.coherentsolutions.l4aichat.s6advanced.dto;

// ChatResponse DTO
public class ChatResponse {
    private String message;
    private String conversationId;

    // Default constructor
    public ChatResponse() {
    }

    // Constructor with message and conversationId
    public ChatResponse(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}