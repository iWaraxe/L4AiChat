package com.coherentsolutions.l4aichat.s3context;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.coherentsolutions.l4aichat.L4AiChatApplication.class)
@AutoConfigureMockMvc
class S3ContextIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldStartNewChatSession() throws Exception {
        mockMvc.perform(post("/api/s3/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello, I'm testing the context management!")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldContinueExistingConversation() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s3/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Remember what I said before?")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldClearConversationMemory() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(delete("/api/s3/chat/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(content().string("Conversation cleared successfully"));
    }

    @Test
    void shouldHandleInMemoryService() throws Exception {
        mockMvc.perform(post("/api/s3/chat/memory/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Testing in-memory service")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists());
    }

    @Test
    void shouldHandleJdbcService() throws Exception {
        mockMvc.perform(post("/api/s3/chat/jdbc/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Testing JDBC service")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists());
    }

    @Test
    void shouldValidateMessageContent() throws Exception {
        mockMvc.perform(post("/api/s3/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(""))) // Empty message
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldValidateConversationId() throws Exception {
        mockMvc.perform(post("/api/s3/chat/invalid-conversation-id!")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleNullMessage() throws Exception {
        mockMvc.perform(post("/api/s3/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // No message field
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldHandleLongMessage() throws Exception {
        // Create a very long message
        String longMessage = "A".repeat(5000);
        
        mockMvc.perform(post("/api/s3/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(longMessage)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Message too long"));
    }

    @Test
    void shouldMaintainContextInMemoryService() throws Exception {
        String conversationId = createTestConversationId();
        
        // First message
        mockMvc.perform(post("/api/s3/chat/memory/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("My name is Alice")))
                .andExpect(status().isOk());
        
        // Second message should have context
        mockMvc.perform(post("/api/s3/chat/memory/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("What's my name?")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId));
    }

    @Test
    void shouldMaintainContextInJdbcService() throws Exception {
        String conversationId = createTestConversationId();
        
        // First message
        mockMvc.perform(post("/api/s3/chat/jdbc/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("My favorite color is blue")))
                .andExpect(status().isOk());
        
        // Second message should have context
        mockMvc.perform(post("/api/s3/chat/jdbc/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("What's my favorite color?")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId));
    }

    @Test
    void shouldHandleSpecialCharactersInMessage() throws Exception {
        String specialMessage = "Hello! How are you? I'm testing émojis: 😀 and symbols: @#$%^&*()";
        
        mockMvc.perform(post("/api/s3/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(specialMessage)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"));
    }
}