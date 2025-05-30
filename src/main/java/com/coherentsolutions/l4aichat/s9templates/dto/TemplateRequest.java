package com.coherentsolutions.l4aichat.s9templates.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public class TemplateRequest {

    @NotBlank(message = "Template name cannot be empty")
    private String templateName;
    
    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    private String message;
    
    private Map<String, String> variables;

    public TemplateRequest() {}

    public TemplateRequest(String templateName, String message) {
        this.templateName = templateName;
        this.message = message;
    }

    // Getters and setters
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "TemplateRequest{" +
                "templateName='" + templateName + '\'' +
                ", message='" + message + '\'' +
                ", variables=" + variables +
                '}';
    }
}