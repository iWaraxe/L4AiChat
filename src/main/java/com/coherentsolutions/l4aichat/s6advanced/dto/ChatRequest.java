package com.coherentsolutions.l4aichat.s6advanced.dto;

// ChatRequest DTO
public class ChatRequest {
    private String message;

    // Default constructor
    public ChatRequest() {
    }

    // Constructor with message
    public ChatRequest(String message) {
        this.message = message;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}