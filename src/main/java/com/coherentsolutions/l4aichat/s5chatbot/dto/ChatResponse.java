package com.coherentsolutions.l4aichat.s5chatbot.dto;

import java.time.LocalDateTime;

public class ChatResponse {

    private String message;
    private String conversationId;
    private LocalDateTime timestamp;

    // Default constructor for JSON serialization
    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.now();
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "message='" + message + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}