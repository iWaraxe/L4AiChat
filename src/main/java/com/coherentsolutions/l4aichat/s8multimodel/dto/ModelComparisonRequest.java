package com.coherentsolutions.l4aichat.s8multimodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ModelComparisonRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String message;
    
    private List<String> models;
    private Double temperature;
    private Integer maxTokens;

    public ModelComparisonRequest() {}

    public ModelComparisonRequest(String message) {
        this.message = message;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public String toString() {
        return "ModelComparisonRequest{" +
                "message='" + message + '\'' +
                ", models=" + models +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                '}';
    }
}