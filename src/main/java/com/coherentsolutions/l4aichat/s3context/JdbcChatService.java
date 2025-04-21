package com.coherentsolutions.l4aichat.s3context;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.jdbc.JdbcChatMemory;
import org.springframework.ai.chat.memory.jdbc.JdbcChatMemoryConfig;
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
        // Build a config for the JDBC memory:
        JdbcChatMemoryConfig config = JdbcChatMemoryConfig.builder()
                .jdbcTemplate(jdbcTemplate)
                // .initializeSchema(true) // if you want auto schema creation
                .build();

        // Initialize JdbcChatMemory
        this.chatMemory = JdbcChatMemory.create(config);

        // Build the ChatClient with our memory advisor
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    You are a friendly and helpful AI assistant that remembers conversation context.
                    Be concise in your responses while still being helpful and accurate.
                    """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
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

        // Pass M7 param names to the advisors
        return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param("chat_memory_conversation_id", finalConvId)
                        .param("chat_memory_response_size", MAX_HISTORY_TOKENS))
                .call()
                .content();
    }

    @Override
    public List<Message> getConversationHistory(String conversationId) {
        return this.chatMemory.get(conversationId,-1);
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