package com.coherentsolutions.l4aichat.s4statemanagement.model;

/**
 * DTO for chat requests from clients.
 */
public record ChatRequest(String message) {

    // For frameworks like Jackson that want a no-arg constructor
    public ChatRequest() {
        this("");
    }
}