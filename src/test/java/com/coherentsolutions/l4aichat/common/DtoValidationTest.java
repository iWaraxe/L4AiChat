package com.coherentsolutions.l4aichat.common;

import com.coherentsolutions.l4aichat.s5chatbot.dto.ChatRequest;
import com.coherentsolutions.l4aichat.s5chatbot.dto.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DTO classes across different modules
 */
class DtoValidationTest {

    @Test
    void testS5ChatRequest() {
        // Test s5 ChatRequest (regular class)
        ChatRequest request = new ChatRequest("Hello, how are you?");
        assertNotNull(request);
        assertEquals("Hello, how are you?", request.getMessage());
        
        ChatRequest emptyRequest = new ChatRequest("");
        assertEquals("", emptyRequest.getMessage());
    }

    @Test
    void testS5ChatResponse() {
        // Test s5 ChatResponse (regular class)
        ChatResponse response = new ChatResponse("Hello there!", "conv-123");
        assertNotNull(response);
        assertEquals("Hello there!", response.getMessage());
        assertEquals("conv-123", response.getConversationId());
        assertNotNull(response.getTimestamp());
        
        // Test no-args constructor
        ChatResponse emptyResponse = new ChatResponse();
        assertNull(emptyResponse.getMessage());
        assertNull(emptyResponse.getConversationId());
        assertNotNull(emptyResponse.getTimestamp());
    }

    @Test
    void testS6AdvancedChatRequest() {
        // Test s6 ChatRequest (record)
        com.coherentsolutions.l4aichat.s6advanced.dto.ChatRequest request = 
            new com.coherentsolutions.l4aichat.s6advanced.dto.ChatRequest("Test message");
        assertNotNull(request);
        assertEquals("Test message", request.message());
    }

    @Test
    void testS6AdvancedChatResponse() {
        // Test s6 ChatResponse (record)
        com.coherentsolutions.l4aichat.s6advanced.dto.ChatResponse response = 
            new com.coherentsolutions.l4aichat.s6advanced.dto.ChatResponse("Hello", "conv-123");
        assertNotNull(response);
        assertEquals("Hello", response.message());
        assertEquals("conv-123", response.conversationId());
        
        // Test no-args constructor
        com.coherentsolutions.l4aichat.s6advanced.dto.ChatResponse emptyResponse = 
            new com.coherentsolutions.l4aichat.s6advanced.dto.ChatResponse();
        assertEquals("", emptyResponse.message());
        assertEquals("", emptyResponse.conversationId());
    }

    @Test
    void testConversationIdGeneration() {
        // Test UUID generation for conversations
        String conversationId1 = java.util.UUID.randomUUID().toString();
        String conversationId2 = java.util.UUID.randomUUID().toString();
        
        assertNotNull(conversationId1);
        assertNotNull(conversationId2);
        assertNotEquals(conversationId1, conversationId2);
        assertTrue(conversationId1.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testMessageValidation() {
        // Test message validation logic
        String validMessage = "Hello, how are you?";
        String emptyMessage = "";
        String nullMessage = null;
        
        assertTrue(isValidMessage(validMessage));
        assertFalse(isValidMessage(emptyMessage));
        assertFalse(isValidMessage(nullMessage));
    }

    private boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty();
    }
}