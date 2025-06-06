package com.coherentsolutions.l4aichat.s4statemanagement.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing conversation state and interactions with the AI model.
 * This service handles:
 * - Maintaining conversation history
 * - Processing messages using Spring AI
 * - Managing conversation lifecycles
 */
@Service
public class ConversationService {

    // Spring AI 1.0.0 param names
    private static final String CONVERSATION_ID_KEY = ChatMemory.CONVERSATION_ID;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    // Maps conversation IDs to their last activity timestamp
    private final ConcurrentHashMap<String, Long> conversationTimestamps = new ConcurrentHashMap<>();

    /**
     * Creates a new ConversationService with the necessary components for stateful chat.
     *
     * @param chatClientBuilder The builder for creating a configured ChatClient
     */
    public ConversationService(ChatClient.Builder chatClientBuilder) {
        // Initialize chat memory store using MessageWindowChatMemory
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(15)
                .build();

        // Build the chat client with memory support via the MessageChatMemoryAdvisor
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                You are a helpful AI assistant.
                Respond in a clear, concise, and friendly manner.
                If you don't know the answer to something, say so rather than making up information.
                """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.chatMemory)
                        .build())
                .build();
    }

    /**
     * Processes a user message in a specific conversation context.
     *
     * @param conversationId The ID of the conversation (created if null)
     * @param userMessage The user's message text
     * @return The AI assistant's response
     */
    public String processMessage(String conversationId, String userMessage) {
        // Generate a new conversation ID if none exists
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = generateConversationId();
        }

        // Update the last activity timestamp
        updateConversationTimestamp(conversationId);

        // Must store in a final var for usage in the advisor lambda
        final String finalConvId = conversationId;

        // Process the message with conversation context
        return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CONVERSATION_ID_KEY, finalConvId))
                .call()
                .content();
    }

    /**
     * Streams responses for a given user message using reactive programming.
     *
     * @param conversationId The ID of the conversation
     * @param userMessage The user's message text
     * @return A Flux of response content chunks
     */
    public Flux<String> streamResponse(String conversationId, String userMessage) {
        // Generate a new conversation ID if none exists
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = generateConversationId();
        }

        // Update the last activity timestamp
        updateConversationTimestamp(conversationId);

        final String finalConvId = conversationId;

        // Stream the response with conversation context
        return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CONVERSATION_ID_KEY, finalConvId))
                .stream()
                .content();
    }

    /**
     * Retrieves the message history for a specific conversation.
     *
     * @param conversationId The ID of the conversation
     * @return A list of messages in the conversation
     */
    public List<Message> getConversationHistory(String conversationId) {
        // In Spring AI 1.0.0, ChatMemory.get() only takes conversation ID
        return this.chatMemory.get(conversationId);
    }

    /**
     * Clears the conversation history for a specific conversation.
     *
     * @param conversationId The ID of the conversation to clear
     */
    public void clearConversation(String conversationId) {
        this.chatMemory.clear(conversationId);
        conversationTimestamps.remove(conversationId);
    }

    /**
     * Generates a new conversation ID.
     *
     * @return A unique conversation identifier
     */
    public String generateConversationId() {
        String conversationId = UUID.randomUUID().toString();
        updateConversationTimestamp(conversationId);
        return conversationId;
    }

    /**
     * Updates the last activity timestamp for a conversation.
     * This is useful for implementing conversation timeout/cleanup.
     *
     * @param conversationId The ID of the conversation
     */
    private void updateConversationTimestamp(String conversationId) {
        conversationTimestamps.put(conversationId, System.currentTimeMillis());
    }

    /**
     * Gets the most recent conversations and their last activity times.
     * Useful for managing conversation lifecycles.
     *
     * @param maxResults Maximum number of conversations to return
     * @return Map of conversation IDs to their last activity timestamp
     */
    public Map<String, Long> getRecentConversations(int maxResults) {
        return conversationTimestamps.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(maxResults)
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        ConcurrentHashMap::new
                ));
    }
}