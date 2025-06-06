package com.coherentsolutions.l4aichat.s3context;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat service implementation using JdbcChatMemory
 * Activated via the "jdbc" profile.
 */
@Service
@Profile("jdbc")
public class JdbcChatService implements ChatService {

    private static final int MAX_HISTORY_TOKENS = 2000;  // For limiting conversation size

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ConcurrentHashMap<String, Long> lastInteractionTimes = new ConcurrentHashMap<>();

    @Autowired
    public JdbcChatService(ChatClient.Builder chatClientBuilder, JdbcTemplate jdbcTemplate) {
        // In Spring AI 1.0.0, JdbcChatMemory is no longer available
        // For now, we'll use MessageWindowChatMemory
        // You could implement a custom ChatMemoryRepository backed by JDBC if needed
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        // Build the ChatClient with our memory advisor
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    You are a friendly and helpful AI assistant that remembers conversation context.
                    Be concise in your responses while still being helpful and accurate.
                    """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.chatMemory)
                        .build())
                .build();
    }

    @Override
    public String processMessage(String conversationId, String userMessage) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        // Keep final variable for the lambda
        final String finalConvId = conversationId;

        // Update last interaction time
        lastInteractionTimes.put(finalConvId, System.currentTimeMillis());

        // Pass conversation ID to the advisors
        return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, finalConvId))
                .call()
                .content();
    }

    @Override
    public List<Message> getConversationHistory(String conversationId) {
        return this.chatMemory.get(conversationId);
    }

    @Override
    public void clearConversation(String conversationId) {
        this.chatMemory.clear(conversationId);
        lastInteractionTimes.remove(conversationId);
    }

    @Override
    public String createNewConversation() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getImplementationType() {
        return "JDBC";
    }
}