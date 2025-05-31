package com.coherentsolutions.l4aichat.s5chatbot;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SpringAiChatbotApplication.class)
@AutoConfigureMockMvc
class S5ChatbotIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldStartNewChatSession() throws Exception {
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello! I'm testing the chatbot.")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldContinueExistingConversation() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s5/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("How are you doing today?")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldStreamChatResponse() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s5/chat/stream/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Tell me a story")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("Connection", "keep-alive"));
    }

    @Test
    void shouldClearConversation() throws Exception {
        String conversationId = createTestConversationId();
        
        // First, create some conversation history
        mockMvc.perform(post("/api/s5/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Remember this message")))
                .andExpect(status().isOk());
        
        // Then clear the conversation
        mockMvc.perform(delete("/api/s5/chat/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Conversation cleared successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldValidateEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(""))) // Empty message
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldValidateNullMessage() throws Exception {
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // No message field
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldValidateMessageLength() throws Exception {
        // Create a very long message exceeding the limit
        String longMessage = "A".repeat(5000);
        
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(longMessage)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Message too long"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldValidateConversationId() throws Exception {
        mockMvc.perform(post("/api/s5/chat/invalid-conversation-id!")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldHandleInvalidJson() throws Exception {
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldHandleMissingContentType() throws Exception {
        mockMvc.perform(post("/api/s5/chat/new")
                .content(createChatRequestJson("Hello"))) // No content type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldHandleSpecialCharactersInMessage() throws Exception {
        String specialMessage = "Hello! Testing émojis: 😀🎉 and symbols: @#$%^&*()[]{}|\\:;\"'<>?/+=";
        
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(specialMessage)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldMaintainConversationContext() throws Exception {
        String conversationId = createTestConversationId();
        
        // First message
        mockMvc.perform(post("/api/s5/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("My name is Bob and I'm a software engineer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // Follow-up message that should use context
        mockMvc.perform(post("/api/s5/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("What's my profession?")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldHandleStreamingErrors() throws Exception {
        String conversationId = createTestConversationId();
        
        // Try to stream with invalid content
        mockMvc.perform(post("/api/s5/chat/stream/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Empty/invalid request
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        String conversationId = createTestConversationId();
        
        // Send multiple requests to the same conversation
        // In a real scenario, this would test thread safety
        mockMvc.perform(post("/api/s5/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("First concurrent message")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        mockMvc.perform(post("/api/s5/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Second concurrent message")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldSupportCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/s5/chat/new")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().string("Access-Control-Allow-Headers", "*"));
    }

    @Test
    void shouldProvideProperErrorStructure() throws Exception {
        mockMvc.perform(post("/api/s5/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(""))) // Invalid request
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.success").value(false));
    }
}