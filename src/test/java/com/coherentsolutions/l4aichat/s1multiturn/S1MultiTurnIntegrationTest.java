package com.coherentsolutions.l4aichat.s1multiturn;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.coherentsolutions.l4aichat.L4AiChatApplication.class)
@AutoConfigureMockMvc
class S1MultiTurnIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldStartNewConversationUsingChatModel() throws Exception {
        mockMvc.perform(post("/api/s1/chat/model/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello, I'm Alice!")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists());
    }

    @Test
    void shouldStartNewConversationUsingChatClient() throws Exception {
        mockMvc.perform(post("/api/s1/chat/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello, I'm Bob!")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").exists());
    }

    @Test
    void shouldContinueConversationUsingChatModel() throws Exception {
        // First, start a conversation
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s1/chat/model/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("What's my name?")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId));
    }

    @Test
    void shouldContinueConversationUsingChatClient() throws Exception {
        // First, start a conversation
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s1/chat/client/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Tell me a joke")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test AI response"))
                .andExpect(jsonPath("$.conversationId").value(conversationId));
    }

    @Test
    void shouldClearConversationMemory() throws Exception {
        String conversationId = createTestConversationId();
        
        mockMvc.perform(post("/api/s1/chat/clear/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(content().string("Conversation memory cleared for: " + conversationId));
    }

    @Test
    void shouldGetChatMessageDemo() throws Exception {
        mockMvc.perform(get("/api/s1/demo/message"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userMessage").exists())
                .andExpect(jsonPath("$.assistantMessage").exists())
                .andExpect(jsonPath("$.systemMessage").exists());
    }

    @Test
    void shouldHandleEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/s1/chat/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidConversationId() throws Exception {
        mockMvc.perform(post("/api/s1/chat/client/invalid-id-with-special-chars!")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createChatRequestJson("Hello")))
                .andExpect(status().isBadRequest());
    }
}