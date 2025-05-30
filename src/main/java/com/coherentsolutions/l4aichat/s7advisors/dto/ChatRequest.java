package com.coherentsolutions.l4aichat.s7advisors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String message;
    
    private String userLocation;
    private String userPreferences;
    private boolean enableContextEnrichment = true;
    private boolean enableContentFilter = true;
    private boolean enableLogging = true;

    public ChatRequest() {}

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

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }

    public boolean isEnableContextEnrichment() {
        return enableContextEnrichment;
    }

    public void setEnableContextEnrichment(boolean enableContextEnrichment) {
        this.enableContextEnrichment = enableContextEnrichment;
    }

    public boolean isEnableContentFilter() {
        return enableContentFilter;
    }

    public void setEnableContentFilter(boolean enableContentFilter) {
        this.enableContentFilter = enableContentFilter;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", userLocation='" + userLocation + '\'' +
                ", userPreferences='" + userPreferences + '\'' +
                ", enableContextEnrichment=" + enableContextEnrichment +
                ", enableContentFilter=" + enableContentFilter +
                ", enableLogging=" + enableLogging +
                '}';
    }
}