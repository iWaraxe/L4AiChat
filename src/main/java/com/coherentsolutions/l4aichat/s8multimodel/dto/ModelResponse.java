package com.coherentsolutions.l4aichat.s8multimodel.dto;

import java.time.LocalDateTime;

public class ModelResponse {

    private String model;
    private String response;
    private long responseTimeMs;
    private int tokensUsed;
    private LocalDateTime timestamp;
    private String error;

    public ModelResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ModelResponse(String model, String response) {
        this.model = model;
        this.response = response;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ModelResponse{" +
                "model='" + model + '\'' +
                ", response='" + response + '\'' +
                ", responseTimeMs=" + responseTimeMs +
                ", tokensUsed=" + tokensUsed +
                ", timestamp=" + timestamp +
                ", error='" + error + '\'' +
                '}';
    }
}