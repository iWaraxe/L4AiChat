package com.coherentsolutions.l4aichat.s8multimodel.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ModelComparisonResponse {

    private String originalMessage;
    private List<ModelResponse> modelResponses;
    private String fastestModel;
    private String shortestResponse;
    private String longestResponse;
    private long totalProcessingTimeMs;
    private LocalDateTime timestamp;

    public ModelComparisonResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ModelComparisonResponse(String originalMessage, List<ModelResponse> modelResponses) {
        this.originalMessage = originalMessage;
        this.modelResponses = modelResponses;
        this.timestamp = LocalDateTime.now();
        calculateStatistics();
    }

    private void calculateStatistics() {
        if (modelResponses == null || modelResponses.isEmpty()) {
            return;
        }

        // Find fastest model
        this.fastestModel = modelResponses.stream()
                .min((r1, r2) -> Long.compare(r1.getResponseTimeMs(), r2.getResponseTimeMs()))
                .map(ModelResponse::getModel)
                .orElse("unknown");

        // Find shortest and longest responses
        this.shortestResponse = modelResponses.stream()
                .filter(r -> r.getResponse() != null)
                .min((r1, r2) -> Integer.compare(r1.getResponse().length(), r2.getResponse().length()))
                .map(ModelResponse::getModel)
                .orElse("unknown");

        this.longestResponse = modelResponses.stream()
                .filter(r -> r.getResponse() != null)
                .max((r1, r2) -> Integer.compare(r1.getResponse().length(), r2.getResponse().length()))
                .map(ModelResponse::getModel)
                .orElse("unknown");

        // Calculate total processing time
        this.totalProcessingTimeMs = modelResponses.stream()
                .mapToLong(ModelResponse::getResponseTimeMs)
                .sum();
    }

    // Getters and setters
    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public List<ModelResponse> getModelResponses() {
        return modelResponses;
    }

    public void setModelResponses(List<ModelResponse> modelResponses) {
        this.modelResponses = modelResponses;
        calculateStatistics();
    }

    public String getFastestModel() {
        return fastestModel;
    }

    public void setFastestModel(String fastestModel) {
        this.fastestModel = fastestModel;
    }

    public String getShortestResponse() {
        return shortestResponse;
    }

    public void setShortestResponse(String shortestResponse) {
        this.shortestResponse = shortestResponse;
    }

    public String getLongestResponse() {
        return longestResponse;
    }

    public void setLongestResponse(String longestResponse) {
        this.longestResponse = longestResponse;
    }

    public long getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }

    public void setTotalProcessingTimeMs(long totalProcessingTimeMs) {
        this.totalProcessingTimeMs = totalProcessingTimeMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ModelComparisonResponse{" +
                "originalMessage='" + originalMessage + '\'' +
                ", modelResponses=" + modelResponses +
                ", fastestModel='" + fastestModel + '\'' +
                ", shortestResponse='" + shortestResponse + '\'' +
                ", longestResponse='" + longestResponse + '\'' +
                ", totalProcessingTimeMs=" + totalProcessingTimeMs +
                ", timestamp=" + timestamp +
                '}';
    }
}