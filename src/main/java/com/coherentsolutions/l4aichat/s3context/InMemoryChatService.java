package com.coherentsolutions.l4aichat.s3context;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat service implementation using InMemoryChatMemory for conversation context
 */
@Service
public class InMemoryChatService implements ChatService {

    private static final int MAX_HISTORY_TOKENS = 2000;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ConcurrentHashMap<String, Long> lastInteractionTimes = new ConcurrentHashMap<>();

    public InMemoryChatService(ChatClient.Builder chatClientBuilder) {
        // Initialize InMemoryChatMemory
        this.chatMemory = new InMemoryChatMemory();

        // Build the ChatClient with our memory advisor and a default system prompt
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

        // 2) Capture it in a final variable
        final String finalConvId = conversationId;

        // Update last interaction time
        lastInteractionTimes.put(conversationId, System.currentTimeMillis());

        // Use the memory advisor by passing the required parameters for M7
        return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param("chat_memory_conversation_id", finalConvId)
                        .param("chat_memory_response_size", MAX_HISTORY_TOKENS)
                )
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
        return "InMemory";
    }
}
