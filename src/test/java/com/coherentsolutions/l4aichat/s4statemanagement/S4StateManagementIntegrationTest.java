package com.coherentsolutions.l4aichat.s4statemanagement;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ChatbotApplication.class)
@AutoConfigureMockMvc
class S4StateManagementIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldStartNewConversation() throws Exception {
        mockMvc.perform(post("/api/s4/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello, this is a new conversation!")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldContinueExistingConversation() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s4/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Continuing our conversation")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldGetConversationHistory() throws Exception {
        String conversationId = createTestConversationId();
        
        // First, send a message to create history
        mockMvc.perform(post("/api/s4/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("First message")))
                .andExpect(status().isOk());
        
        // Then get the history
        mockMvc.perform(get("/api/s4/chat/history/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.totalMessages").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldClearConversation() throws Exception {
        String conversationId = createTestConversationId();
        
        // First, create some conversation history
        mockMvc.perform(post("/api/s4/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Message to be cleared")))
                .andExpect(status().isOk());
        
        // Then clear the conversation
        mockMvc.perform(delete("/api/s4/chat/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(content().string("Conversation cleared successfully"));
        
        // Verify history is empty
        mockMvc.perform(get("/api/s4/chat/history/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isEmpty())
                .andExpect(jsonPath("$.totalMessages").value(0));
    }

    @Test
    void shouldHandleConversationCleaning() throws Exception {
        // Test the conversation cleaning endpoint
        mockMvc.perform(post("/api/s4/admin/clean-conversations")
                .param("olderThanHours", "24"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cleanedCount").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetSystemStats() throws Exception {
        mockMvc.perform(get("/api/s4/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalConversations").exists())
                .andExpect(jsonPath("$.activeConversations").exists())
                .andExpect(jsonPath("$.totalMessages").exists())
                .andExpect(jsonPath("$.averageMessagesPerConversation").exists());
    }

    @Test
    void shouldValidateMessageInput() throws Exception {
        mockMvc.perform(post("/api/s4/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson(""))) // Empty message
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldValidateConversationId() throws Exception {
        mockMvc.perform(post("/api/s4/chat/invalid-id-with-special-characters!")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleNonExistentConversationHistory() throws Exception {
        String nonExistentId = "non-existent-conversation-id";
        
        mockMvc.perform(get("/api/s4/chat/history/" + nonExistentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.conversationId").value(nonExistentId))
                .andExpect(jsonPath("$.messages").isEmpty())
                .andExpect(jsonPath("$.totalMessages").value(0));
    }

    @Test
    void shouldMaintainConversationState() throws Exception {
        String conversationId = createTestConversationId();
        
        // Send first message
        mockMvc.perform(post("/api/s4/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("My name is Alice and I like programming")))
                .andExpect(status().isOk());
        
        // Send second message
        mockMvc.perform(post("/api/s4/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("What do you know about me?")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test AI response"));
        
        // Check that history contains both messages
        mockMvc.perform(get("/api/s4/chat/history/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.totalMessages").value(4)); // 2 user + 2 assistant messages
    }

    @Test
    void shouldHandleMultipleConversations() throws Exception {
        String conversationId1 = createTestConversationId() + "-1";
        String conversationId2 = createTestConversationId() + "-2";
        
        // Create first conversation
        mockMvc.perform(post("/api/s4/chat/" + conversationId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("First conversation message")))
                .andExpect(status().isOk());
        
        // Create second conversation
        mockMvc.perform(post("/api/s4/chat/" + conversationId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Second conversation message")))
                .andExpect(status().isOk());
        
        // Verify both conversations exist separately
        mockMvc.perform(get("/api/s4/chat/history/" + conversationId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId1))
                .andExpect(jsonPath("$.totalMessages").value(2));
        
        mockMvc.perform(get("/api/s4/chat/history/" + conversationId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId2))
                .andExpect(jsonPath("$.totalMessages").value(2));
    }

    @Test
    void shouldHandleConversationCleaningWithTimeFilter() throws Exception {
        // Create a conversation
        String conversationId = createTestConversationId();
        mockMvc.perform(post("/api/s4/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Test message for cleaning")))
                .andExpect(status().isOk());
        
        // Clean conversations older than 0 hours (should clean all)
        mockMvc.perform(post("/api/s4/admin/clean-conversations")
                .param("olderThanHours", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cleanedCount").exists());
    }
}