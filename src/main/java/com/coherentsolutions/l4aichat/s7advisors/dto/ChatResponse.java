package com.coherentsolutions.l4aichat.s7advisors.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    private String message;
    private String conversationId;
    private LocalDateTime timestamp;
    private List<String> advisorsUsed;
    private boolean contentFiltered;
    private long processingTimeMs;

    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.now();
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getAdvisorsUsed() {
        return advisorsUsed;
    }

    public void setAdvisorsUsed(List<String> advisorsUsed) {
        this.advisorsUsed = advisorsUsed;
    }

    public boolean isContentFiltered() {
        return contentFiltered;
    }

    public void setContentFiltered(boolean contentFiltered) {
        this.contentFiltered = contentFiltered;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "message='" + message + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", timestamp=" + timestamp +
                ", advisorsUsed=" + advisorsUsed +
                ", contentFiltered=" + contentFiltered +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}